package com.example.spotifyraul.BBDD

import com.example.spotifyraul.R


data class Song(
    val name: String,
    val coverResourceId: Int,
    val cancion: Int,
    val duration: String
)

object BBDD {
    val ListaCanciones: List<Song> = listOf(
        Song("Cacho a cacho - Estopa", R.drawable.estopa, R.raw.estopa, "02:34"),
        Song("Monster Trio 「 AMV 」", R.drawable.monster, R.raw.monster, "014:10"),
        Song("Mordecai y los Rigbys - Party Tonight", R.drawable.mordecai, R.raw.mordecai, "03:16"),
        Song("Minero ft Rubius", R.drawable.minero, R.raw.minero, "3:53"),
        Song("Yo tengo un moco", R.drawable.moco, R.raw.moco, "00:47"),
        Song("Me pico un mosquito", R.drawable.mosquito, R.raw.mosquito, "02:18"),
        Song("Rayden - Solo los amantes sobreviven", R.drawable.rayden, R.raw.rayden, "03:13"),
        Song(
            "Santa RM - Mucho Para Mí (Ft. Franco Escamilla)",
            R.drawable.santa,
            R.raw.santa,
            "05:10"
        ),
        Song("Windows Error Remix", R.drawable.windows, R.raw.windows, "03:21"),
        Song(
            "Arctic Monkeys & Lana del Rey - I Wanna be Yours, Summertime Sadness",
            R.drawable.morir,
            R.raw.morir,
            "03:05"
        )
    )
}