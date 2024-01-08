package com.example.spotifyraul.ExoPlayer

import android.content.ContentResolver
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.spotifyraul.BBDD.BBDD.ListaCanciones
import com.example.spotifyraul.BBDD.Song
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PrincipalViewModel : ViewModel() {

    private val _exoPlayer: MutableStateFlow<ExoPlayer?> = MutableStateFlow(null)
    val exoPlayer = _exoPlayer.asStateFlow()

    private val _isLoopMode: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isLoopMode = _isLoopMode.asStateFlow()

    private val _isRandomMode: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isRandomMode = _isRandomMode.asStateFlow()

    private val _currentSongIndex: MutableStateFlow<Int> = MutableStateFlow(0)
    val currentSongIndex = _currentSongIndex.asStateFlow()

    private val _actual = MutableStateFlow(ListaCanciones[_currentSongIndex.value].cancion)
    val actual = _actual.asStateFlow()

    private val _duracion = MutableStateFlow(0)
    val duracion = _duracion.asStateFlow()

    private val _progreso = MutableStateFlow(0)
    val progreso = _progreso.asStateFlow()


    fun crearExoPlayer(context: Context) {
        _exoPlayer.value = ExoPlayer.Builder(context).build()
        _exoPlayer.value!!.prepare()
    }

    fun hacerSonarMusica(context: Context) {
        val mediaItem = MediaItem.fromUri(obtenerRuta(context, _currentSongIndex.value))

        _exoPlayer.value!!.setMediaItem(mediaItem)
        _exoPlayer.value!!.prepare()
        _exoPlayer.value!!.playWhenReady = true
    }

    private fun handlePlaybackStateChanged(playbackState: Int, context: Context) {
        if (playbackState == Player.STATE_READY) {
            _duracion.value = (_exoPlayer.value!!.duration / 1000).toInt()
            viewModelScope.launch {
                while (_exoPlayer.value!!.isPlaying) {
                    _progreso.value = (_exoPlayer.value!!.currentPosition / 1000).toInt()
                    delay(1000)
                }
            }
        } else if (playbackState == Player.STATE_ENDED) {
            playNext(context)
        }
    }

    private fun obtenerRuta(context: Context, songIndex: Int): String {
        setCurrentSongIndex(songIndex)

        val currentSong = getCurrentSong().cancion

        return ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                context.packageName + '/' + context.resources.getResourceTypeName(currentSong) + '/' +
                context.resources.getResourceEntryName(currentSong)
    }

    fun getCurrentSong(): Song {
        return ListaCanciones[_currentSongIndex.value]
    }

    fun getNextSongIndex(): Int {
        return if (_isRandomMode.value) {
            (0 until ListaCanciones.size).random()
        } else {
            (_currentSongIndex.value + 1) % ListaCanciones.size
        }
    }

    fun toggleLoopMode() {
        _isLoopMode.value = !_isLoopMode.value

        if (_isLoopMode.value) {
            // Si el bucle está activado, configura el bucle
            _exoPlayer.value?.repeatMode = Player.REPEAT_MODE_ONE
        } else {
            // Si el bucle está desactivado, restablece el modo de bucle
            _exoPlayer.value?.repeatMode = Player.REPEAT_MODE_OFF
        }

        // También desactiva el modo aleatorio cuando se activa el bucle
        if (_isLoopMode.value) {
            _isRandomMode.value = false
        }
    }

    fun toggleRandomMode() {
        _isRandomMode.value = !_isRandomMode.value

        // Si la reproducción aleatoria está activada, configura el modo de reproducción aleatoria
        if (_isRandomMode.value) {
            _exoPlayer.value?.shuffleModeEnabled = true
            _exoPlayer.value?.repeatMode = Player.REPEAT_MODE_OFF
        } else {
            // Si la reproducción aleatoria está desactivada, restablece el modo de reproducción aleatoria
            _exoPlayer.value?.shuffleModeEnabled = false
        }

        // También desactiva el modo de bucle cuando se activa la reproducción aleatoria
        if (_isRandomMode.value) {
            _isLoopMode.value = false
        }
    }

    fun setCurrentSongIndex(index: Int) {
        _currentSongIndex.value = index
    }

    fun inicializarEP(context: Context) {
        _exoPlayer.value = ExoPlayer.Builder(context).build()
        _exoPlayer.value!!.prepare()
        _exoPlayer.value!!.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                handlePlaybackStateChanged(playbackState, context)
            }
        })
    }

    override fun onCleared() {
        _exoPlayer.value?.release()
        super.onCleared()
    }

    fun pausarOSeguirMusica(): Boolean {
        return if (_exoPlayer.value!!.isPlaying) {
            _exoPlayer.value!!.pause()
            true // Devuelve true si la música está en pausa
        } else {
            _exoPlayer.value!!.play()
            false // Devuelve false si la música se está reproduciendo
        }
    }

    fun playPrevious(context: Context) {
        setCurrentSongIndex((_currentSongIndex.value - 1 + ListaCanciones.size) % ListaCanciones.size)

        // Cambia la canción actual y la imagen
        _actual.value = ListaCanciones[_currentSongIndex.value].cancion

        // Prepara y reproduce la nueva canción
        val mediaItem = MediaItem.fromUri(obtenerRuta(context, _currentSongIndex.value))
        _exoPlayer.value?.setMediaItem(mediaItem)
        _exoPlayer.value?.prepare()

        if (_isLoopMode.value) {
            // Si el bucle está activado, configura el bucle
            _exoPlayer.value?.repeatMode = Player.REPEAT_MODE_ONE
        }

        _exoPlayer.value?.playWhenReady = true
    }

    fun playNext(context: Context) {
        val nextSongIndex = if (_isRandomMode.value) {
            (0 until ListaCanciones.size).random()
        } else {
            (_currentSongIndex.value + 1) % ListaCanciones.size
        }

        setCurrentSongIndex(nextSongIndex)

        // Cambia la canción actual y la imagen
        _actual.value = ListaCanciones[_currentSongIndex.value].cancion

        // Prepara y reproduce la nueva canción
        val mediaItem = MediaItem.fromUri(obtenerRuta(context, _currentSongIndex.value))
        _exoPlayer.value?.setMediaItem(mediaItem)
        _exoPlayer.value?.prepare()

        // Configura el modo de reproducción aleatoria si está activado
        if (_isRandomMode.value) {
            _exoPlayer.value?.shuffleModeEnabled = true
            _exoPlayer.value?.repeatMode = Player.REPEAT_MODE_OFF
        }

        // Configura el modo de bucle si está activado
        if (_isLoopMode.value) {
            _exoPlayer.value?.repeatMode = Player.REPEAT_MODE_ONE
        }

        _exoPlayer.value?.playWhenReady = true
    }

    fun seekTo(positionMillis: Long) {
        _exoPlayer.value?.seekTo(positionMillis)
    }

    fun playSong(context: Context, songIndex: Int) {
        setCurrentSongIndex(songIndex)

        val mediaItem = MediaItem.fromUri(obtenerRuta(context, songIndex))
        _exoPlayer.value?.setMediaItem(mediaItem)
        _exoPlayer.value?.prepare()
        _exoPlayer.value?.playWhenReady = true
    }
}