package PantallaCancion

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spotifyraul.BBDD.BBDD
import com.example.spotifyraul.ExoPlayer.PrincipalViewModel
import com.example.spotifyraul.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Cancion(ExoPlayer: PrincipalViewModel = viewModel()) {
    var isPlaying by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    var isSearchVisible by remember { mutableStateOf(false) }
    var isArrowBackVisible by remember { mutableStateOf(true) }
    var menuDesplegado by remember { mutableStateOf(false) }
    var seleccionCancion by remember { mutableStateOf<String?>(null) }
    var isTextVisible by remember { mutableStateOf(true) } // Variable para controlar la visibilidad del texto "Spotify"

    val currentSongIndex by ExoPlayer.currentSongIndex.collectAsState()
    val currentSong = ExoPlayer.getCurrentSong().name
    val contexto = LocalContext.current

    val reproductor = ExoPlayer.exoPlayer.collectAsState().value

    if (reproductor == null) {
        ExoPlayer.inicializarEP(context = contexto)
        ExoPlayer.hacerSonarMusica(contexto)
    }

    DisposableEffect(seleccionCancion) {
        if (seleccionCancion != null) {
            // Lógica para reproducir la canción seleccionada
            val index = BBDD.ListaCanciones.indexOfFirst { it.name == seleccionCancion }
            if (index != -1) {
                ExoPlayer.playSong(contexto, index)
                isPlaying = true
            }
            seleccionCancion = null // Reinicia la selección después de reproducir la canción
        }
        onDispose { }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isTextVisible) {
                        Text(text = "Spotify")
                    }
                },
                navigationIcon = {
                    if (isArrowBackVisible) {
                        IconButton(onClick = { /* Navigate back */ }) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                        }
                    }
                },
                actions = {

                }
            )
        },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
            ) {
                // Icono de búsqueda en la parte superior
                if (isSearchVisible) {
                    Column(modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)) {
                        SearchBar(
                            query = searchText,
                            onQueryChange = {
                                searchText = it
                                if (it.isEmpty()) {
                                    menuDesplegado = false
                                    seleccionCancion = null
                                }
                            },
                            onSearch = {
                                if (seleccionCancion == null) {
                                    menuDesplegado = false
                                }
                            },
                            active = menuDesplegado,
                            onActiveChange = { menuDesplegado = !menuDesplegado },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            BBDD.ListaCanciones.forEach { cancion ->
                                if (cancion.name.startsWith(searchText, ignoreCase = true)) {
                                    DropdownMenuItem(
                                        onClick = {
                                            seleccionCancion = cancion.name
                                            searchText = cancion.name
                                            menuDesplegado = false
                                        },
                                        text = { Text(text = cancion.name) }
                                    )
                                }
                            }
                        }
                    }
                }
                // Mostrar información de la canción arriba de la imagen
                Text(
                    text = "Now Playing: ",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(6.dp)
                )
                Text(
                    text = ExoPlayer.getCurrentSong().name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(5.dp)
                        .align(Alignment.CenterHorizontally)
                )

                // Mostrar la carátula de la canción en el medio de la pantalla, más pequeña
                Image(
                    painter = painterResource(
                        id = BBDD.ListaCanciones.getOrNull(currentSongIndex)?.coverResourceId ?: 0
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .padding(vertical = 16.dp),
                    contentScale = ContentScale.Crop
                )

                // Slider de duración
                val progreso = ExoPlayer.progreso.collectAsState().value.toFloat()
                val duracionTotal = ExoPlayer.duracion.collectAsState().value.toFloat()

                // Slider de duración
                Slider(
                    value = ExoPlayer.progreso.collectAsState().value.toFloat(),
                    onValueChange = {
                        ExoPlayer.seekTo(it.toLong() * 1000)
                    },
                    valueRange = 0f..ExoPlayer.duracion.collectAsState().value.toFloat(),
                    steps = 100,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                        .fillMaxWidth()
                        .widthIn(0.dp, 300.dp)
                )

                // Textos para mostrar el progreso actual y la duración total
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = formatTime(progreso.toLong()))
                    Text(text = formatTime(duracionTotal.toLong()))
                }

                // Botones adicionales
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButton(
                        onClick = {
                            ExoPlayer.toggleRandomMode()
                        }
                    ) {
                        val icon = if (ExoPlayer.isRandomMode.collectAsState().value) {
                            Icons.Default.Replay
                        } else {
                            Icons.Default.Shuffle
                        }
                        Icon(imageVector = icon, contentDescription = null)
                    }

                    IconButton(
                        onClick = {
                            ExoPlayer.playPrevious(contexto)
                        }
                    ) {
                        Icon(imageVector = Icons.Default.SkipPrevious, contentDescription = null)
                    }

                    IconButton(
                        onClick = {
                            isPlaying = !isPlaying
                            ExoPlayer.pausarOSeguirMusica()
                        }
                    ) {
                        val icon = if (isPlaying) {
                            Icons.Default.Pause
                        } else {
                            Icons.Default.PlayArrow
                        }
                        Icon(imageVector = icon, contentDescription = null)
                    }

                    IconButton(
                        onClick = {
                            ExoPlayer.playNext(contexto)
                        }
                    ) {
                        Icon(imageVector = Icons.Default.SkipNext, contentDescription = null)
                    }

                    IconButton(
                        onClick = {
                            ExoPlayer.toggleLoopMode()
                        }
                    ) {
                        val icon = if (ExoPlayer.isLoopMode.collectAsState().value) {
                            Icons.Default.RepeatOne
                        } else {
                            Icons.Default.Repeat
                        }
                        Icon(imageVector = icon, contentDescription = null)
                    }
                }
            }
        },
        bottomBar = {
            BottomAppBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp), // Ajusta el espaciado según tus necesidades
                contentPadding = PaddingValues(16.dp)
            ) {
                // Íconos de la barra inferior
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = { R.drawable.nocancion }
                    ) {
                        Icon(imageVector = Icons.Default.Home, contentDescription = "Home")
                    }
                    IconButton(
                        onClick = {
                            // Mostrar u ocultar el campo de búsqueda en la parte superior al hacer clic en el ícono de búsqueda en la barra inferior
                            isSearchVisible = !isSearchVisible
                            // Ocultar la flecha hacia atrás cuando se muestra la barra de búsqueda
                            isArrowBackVisible = !isSearchVisible
                            // Ocultar el texto de "Spotify"
                            isTextVisible = !isSearchVisible
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                    }
                }
            }
        }
    )
}

// Función auxiliar para formatear el tiempo en formato MM:SS
@Composable
fun formatTime(seconds: Long): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "%02d:%02d".format(minutes, remainingSeconds)
}