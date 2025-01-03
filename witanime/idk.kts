version = 12

cloudstream {
    language = "ar"
    // All of these properties are optional, you can safely remove them

    description = "Movies and Series upto 4K"
     authors = listOf("megix")

    /**
     * Status int as the following:
     * 0: Down
     * 1: Ok
     * 2: Slow
     * 3: Beta only
     * */
    status = 1 // will be 3 if unspecified
    tvTypes = listOf(
        "Movie",
        "Anime"
    )

    iconUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRnK2t1TkmQMH2os2dpZBZmzfzSfm71MnwCeg&s"
}