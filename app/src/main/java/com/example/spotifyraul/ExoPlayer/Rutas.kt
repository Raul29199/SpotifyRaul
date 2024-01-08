package com.example.spotifyraul.ExoPlayer

sealed class Rutas(val ruta: String) {
    object Player : Rutas("player")
    object Foto : Rutas("foto")
}