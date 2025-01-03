package com.example

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import com.lagradost.cloudstream3.base64Decode
import com.lagradost.cloudstream3.LoadResponse.Companion.addImdbUrl
import com.lagradost.cloudstream3.LoadResponse.Companion.addActors
import com.google.gson.Gson
import com.lagradost.cloudstream3.utils.AppUtils.parseJson

class WitanimeProvider : MainAPI() {
    override var mainUrl = "https://witanime.quest"
    override var name = "Witanime"
    override val hasMainPage = true
    override var lang = "ar"
    override val hasDownloadSupport = true
    override val supportedTypes = setOf(
        TvType.Anime,
        TvType.Movie
    )

    override val mainPage = mainPageOf(
        "$mainUrl/" to "Home",
        "$mainUrl/anime/" to "Anime",
        "$mainUrl/movies/" to "Movies"
    )

    override suspend fun getMainPage(
        page: Int,
        request: MainPageRequest
    ): HomePageResponse {
        val document = if(page == 1) {
            app.get(request.data).document
        } else {
            app.get(request.data + "page/" + page).document
        }
        val home = document.select("div.anime-card-container").mapNotNull {
            it.toSearchResult()
        }
        return newHomePageResponse(request.name, home)
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val title = this.select("a").attr("title")
        val href = this.select("a").attr("href")
        val posterUrl = this.select("img").attr("data-src")

        return newAnimeSearchResponse(title, href, TvType.Anime) {
            this.posterUrl = posterUrl
        }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val searchResponse = mutableListOf<SearchResponse>()

        for (i in 1..6) {
            val document = app.get("$mainUrl/?search_param=animes&s=$query&page=$i").document

            val results = document.select("div.anime-card-container").mapNotNull { it.toSearchResult() }

            if (results.isEmpty()) {
                break
            }
            searchResponse.addAll(results)
        }

        return searchResponse
    }

    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url).document
        val title = document.selectFirst("h1.anime-title")?.text().toString()
        val posterUrl = document.selectFirst("img.anime-thumbnail")?.attr("src").toString()
        val description = document.selectFirst("div.anime-description")?.text().toString()
        val tvtype = if (url.contains("/movies/")) {
            TvType.Movie
        } else {
            TvType.Anime
        }

        val data = document.select("a.dl").map {
            EpisodeLink(it.attr("href"))
        }

        return newAnimeLoadResponse(title, url, tvtype, data) {
            this.posterUrl = posterUrl
            this.plot = description
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val sources = parseJson<ArrayList<EpisodeLink>>(data)
        sources.amap {
            val source = it.source
            loadExtractor(source, subtitleCallback, callback)
        }
        return true
    }

    data class EpisodeLink(
        val source: String
    )
}