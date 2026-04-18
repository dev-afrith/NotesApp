@file:OptIn(
    androidx.compose.foundation.ExperimentalFoundationApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class
)

package com.example.notesapp.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.notesapp.data.Note
import com.example.notesapp.viewmodel.NoteViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ── Palette ───────────────────────────────────────────────────────────────────
private val BgDeep = Color(0xFF07070F)
private val GlassFill = Color(0x18FFFFFF)
private val GlassBorder = Color(0x2EFFFFFF)
private val Gold = Color(0xFFFFCC00)
private val Cyan = Color(0xFF00E5FF)
private val Violet = Color(0xFFB388FF)
private val Emerald = Color(0xFF00E676)
private val Coral = Color(0xFFFF6B6B)
private val MenuBg = Color(0xFF181825)

// Per-note accent: no pink/rose — replaced with distinct premium hues
private val NoteAccents = listOf(
    Pair(Color(0xFF1A2744), Gold),      // Deep navy  / gold
    Pair(Color(0xFF0D2B2B), Cyan),      // Deep teal  / cyan
    Pair(Color(0xFF1E1535), Violet),    // Deep plum  / violet
    Pair(Color(0xFF0D2318), Emerald),   // Deep forest/ emerald
    Pair(Color(0xFF2B1A1A), Coral),     // Deep brown / coral
    Pair(Color(0xFF1A1A2E), Color(0xFFAB47BC)), // Midnight/ purple
)

// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NotesScreen(viewModel: NoteViewModel) {
    val notes by viewModel.notes.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val recentlyDeleted by viewModel.recentlyDeleted.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showBottomSheet by remember { mutableStateOf(false) }
    var editingNote by remember { mutableStateOf<Note?>(null) }
    var noteToDelete by remember { mutableStateOf<Note?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var fullScreenImages by remember { mutableStateOf<List<String>?>(null) }
    var fullScreenStartIndex by remember { mutableStateOf(0) }

    LaunchedEffect(recentlyDeleted) {
        recentlyDeleted?.let {
            val result = snackbarHostState.showSnackbar(
                message = "Note deleted",
                actionLabel = "Undo",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) viewModel.undoDelete()
            else viewModel.clearRecentlyDeleted()
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog && noteToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false; noteToDelete = null },
            title = { Text("Delete Note", fontWeight = FontWeight.Bold, color = Color.White) },
            text = {
                Text(
                    "This note will be permanently deleted.",
                    color = Color.White.copy(alpha = 0.7f)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    noteToDelete?.let { viewModel.deleteNote(it) }
                    showDeleteDialog = false; noteToDelete = null
                }) { Text("Delete", color = Coral, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false; noteToDelete = null }) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.5f))
                }
            },
            containerColor = MenuBg,
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Full-screen image viewer
    fullScreenImages?.let { imgs ->
        FullScreenImageViewer(
            images = imgs,
            startIndex = fullScreenStartIndex,
            onDismiss = { fullScreenImages = null }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeep)
    ) {
        MeshBackground()

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = Color.Transparent,
            floatingActionButton = {
                PremiumFAB(onClick = { editingNote = null; showBottomSheet = true })
            }
        ) { paddingValues ->
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = paddingValues.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding() + 100.dp
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Spacer(modifier = Modifier.statusBarsPadding())
                    PremiumHeader()
                    Spacer(modifier = Modifier.height(14.dp))
                    PremiumSearchBar(
                        query = searchQuery,
                        onQueryChange = { viewModel.setSearchQuery(it) }
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                if (notes.isEmpty()) {
                    item { EmptyState() }
                } else {
                    itemsIndexed(items = notes, key = { _, n -> n.id }) { index, note ->

                        // Staggered entrance
                        val alpha = remember { Animatable(0f) }
                        val slide = remember { Animatable(50f) }
                        LaunchedEffect(Unit) {
                            kotlinx.coroutines.delay(index * 55L)
                            launch { alpha.animateTo(1f, tween(380, easing = FastOutSlowInEasing)) }
                            launch { slide.animateTo(0f, tween(380, easing = FastOutSlowInEasing)) }
                        }

                        Box(
                            modifier = Modifier.graphicsLayer {
                                this.alpha = alpha.value
                                translationY = slide.value
                            }
                        ) {
                            NoteCard(
                                note = note,
                                accentPair = NoteAccents[note.id % NoteAccents.size],
                                onClick = { editingNote = note; showBottomSheet = true },
                                onEdit = { editingNote = note; showBottomSheet = true },
                                onDelete = { noteToDelete = note; showDeleteDialog = true },
                                onPin = { viewModel.togglePin(note) },
                                onImageClick = { imgs, idx ->
                                    fullScreenImages = imgs
                                    fullScreenStartIndex = idx
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showBottomSheet) {
        NoteBottomSheet(
            note = editingNote,
            onDismiss = { showBottomSheet = false },
            onSave = { t, c, imgs ->
                if (editingNote != null)
                    viewModel.updateNote(editingNote!!.copy(title = t, content = c, images = imgs))
                else
                    viewModel.addNote(t, c, imgs)
                showBottomSheet = false
            }
        )
    }
}

// ─── Mesh BG ──────────────────────────────────────────────────────────────────
@Composable
fun MeshBackground() {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.TopEnd)
                .graphicsLayer { translationX = 100f; translationY = -80f }
                .blur(130.dp)
                .background(
                    Brush.radialGradient(listOf(Color(0x28FFCC00), Color.Transparent)),
                    CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(260.dp)
                .align(Alignment.BottomStart)
                .graphicsLayer { translationX = -80f; translationY = 80f }
                .blur(110.dp)
                .background(
                    Brush.radialGradient(listOf(Color(0x2000E5FF), Color.Transparent)),
                    CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(220.dp)
                .align(Alignment.Center)
                .blur(90.dp)
                .background(
                    Brush.radialGradient(listOf(Color(0x18B388FF), Color.Transparent)),
                    CircleShape
                )
        )
    }
}

// ─── Header ───────────────────────────────────────────────────────────────────
@Composable
fun PremiumHeader() {
    Column {
        Text(
            text = "My Notes",
            fontSize = 44.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            letterSpacing = (-1.5).sp
        )
        Text(
            text = "Your thoughts, beautifully kept",
            fontSize = 13.sp,
            fontWeight = FontWeight.Light,
            color = Color.White.copy(alpha = 0.35f),
            letterSpacing = 0.4.sp
        )
    }
}

// ─── Search ───────────────────────────────────────────────────────────────────
@Composable
fun PremiumSearchBar(query: String, onQueryChange: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(GlassFill)
            .drawBehind {
                drawRoundRect(
                    color = GlassBorder,
                    cornerRadius = CornerRadius(22.dp.toPx()),
                    style = Stroke(width = 1f)
                )
            }
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = {
                Text("Search notes…", color = Color.White.copy(alpha = 0.3f), fontSize = 15.sp)
            },
            leadingIcon = {
                Icon(Icons.Default.Search, null, tint = Color.White.copy(alpha = 0.4f))
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Default.Close, null, tint = Color.White.copy(alpha = 0.4f))
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ─── Note Card ────────────────────────────────────────────────────────────────
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteCard(
    note: Note,
    accentPair: Pair<Color, Color>,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onPin: () -> Unit,
    onImageClick: (List<String>, Int) -> Unit
) {
    val (cardBg, accent) = accentPair
    var menuExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(cardBg, cardBg.copy(alpha = 0.85f)),
                    start = Offset(0f, 0f),
                    end = Offset(800f, 800f)
                )
            )
            .background(GlassFill)
            .drawBehind {
                drawRoundRect(
                    color = GlassBorder,
                    cornerRadius = CornerRadius(26.dp.toPx()),
                    style = Stroke(width = 1.1f)
                )
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(accent.copy(alpha = 0.9f), accent.copy(alpha = 0.2f))
                    ),
                    topLeft = Offset(0f, size.height * 0.08f),
                    size = Size(4.dp.toPx(), size.height * 0.84f),
                    cornerRadius = CornerRadius(4.dp.toPx())
                )
            }
            .combinedClickable(onClick = onClick, onLongClick = onPin)
    ) {
        // Top pin stripe
        if (note.isPinned) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp)
                    .clip(RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Gold.copy(alpha = 0.9f),
                                Violet.copy(alpha = 0.6f)
                            )
                        )
                    )
            )
        }

        Column(
            modifier = Modifier.padding(start = 20.dp, end = 8.dp, top = 18.dp, bottom = 18.dp)
        ) {
            // Title row + 3-dot menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    if (note.title.isNotBlank()) {
                        Text(
                            text = note.title,
                            fontSize = 21.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = (-0.2).sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Three-dot menu ONLY — no delete icon anywhere
                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Menu",
                            tint = Color.White
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier
                            .background(MenuBg, RoundedCornerShape(16.dp))
                            .width(180.dp)
                    ) {
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Edit,
                                        null,
                                        tint = Cyan,
                                        modifier = Modifier.size(17.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text("Edit", color = Color.White, fontSize = 15.sp)
                                }
                            },
                            onClick = { menuExpanded = false; onEdit() },
                            colors = MenuDefaults.itemColors(textColor = Color.White)
                        )

                        HorizontalDivider(color = Color.White.copy(alpha = 0.07f))

                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        if (note.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                                        null, tint = Gold, modifier = Modifier.size(17.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        if (note.isPinned) "Unpin" else "Pin",
                                        color = Color.White, fontSize = 15.sp
                                    )
                                }
                            },
                            onClick = { menuExpanded = false; onPin() },
                            colors = MenuDefaults.itemColors(textColor = Color.White)
                        )

                        HorizontalDivider(color = Color.White.copy(alpha = 0.07f))

                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Delete,
                                        null,
                                        tint = Coral,
                                        modifier = Modifier.size(17.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text("Delete", color = Coral, fontSize = 15.sp)
                                }
                            },
                            onClick = { menuExpanded = false; onDelete() },
                            colors = MenuDefaults.itemColors(textColor = Coral)
                        )
                    }
                }
            }

            // Content
            if (note.content.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = note.content,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.65f),
                    lineHeight = 21.sp,
                    maxLines = 7,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Images — no badge/chip, just clean thumbnails
            if (note.images.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(note.images.take(4)) { idx, uri ->
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(Uri.parse(uri)).crossfade(true).build(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .combinedClickable(onClick = { onImageClick(note.images, idx) })
                            )
                            // Only show +N overlay on last thumb if more than 4
                            if (idx == 3 && note.images.size > 4) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.55f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "+${note.images.size - 4}",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Date only — no chips, no tags
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = formatNoteDate(note.createdAt),
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.28f),
                modifier = Modifier.padding(end = 4.dp)
            )
        }
    }
}

fun formatNoteDate(ts: Long): String =
    SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(ts))

// ─── Swipe background ─────────────────────────────────────────────────────────
@Composable
fun SwipeBg() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 2.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0x00FF6B6B), Color(0x99FF6B6B))
                )
            ),
        contentAlignment = Alignment.CenterEnd
    ) {
        Icon(
            Icons.Default.Delete,
            null,
            tint = Color.White,
            modifier = Modifier.padding(end = 24.dp)
        )
    }
}

// ─── FAB ──────────────────────────────────────────────────────────────────────
@Composable
fun PremiumFAB(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(62.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(listOf(Gold, Color(0xFFFFA040))))
            .combinedClickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "New Note",
            tint = Color(0xFF1A1000),
            modifier = Modifier.size(30.dp)
        )
    }
}

// ─── Full-screen viewer ───────────────────────────────────────────────────────
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FullScreenImageViewer(images: List<String>, startIndex: Int, onDismiss: () -> Unit) {
    val pagerState = rememberPagerState(initialPage = startIndex, pageCount = { images.size })

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                var scale by remember { mutableFloatStateOf(1f) }
                var offsetX by remember { mutableFloatStateOf(0f) }
                var offsetY by remember { mutableFloatStateOf(0f) }

                val txState = rememberTransformableState { zoom, pan, _ ->
                    scale = (scale * zoom).coerceIn(1f, 5f)
                    if (scale > 1f) {
                        offsetX += pan.x; offsetY += pan.y
                    } else {
                        offsetX = 0f; offsetY = 0f
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .transformable(state = txState)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onDoubleTap = {
                                    if (scale > 1.5f) {
                                        scale = 1f; offsetX = 0f; offsetY = 0f
                                    } else scale = 2.5f
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(Uri.parse(images[page])).crossfade(true).build(),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = scale; scaleY = scale
                                translationX = offsetX; translationY = offsetY
                            }
                    )
                }
            }

            // Close
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(16.dp)
                    .size(40.dp)
                    .background(Color.White.copy(alpha = 0.12f), CircleShape)
            ) {
                Icon(Icons.Default.Close, null, tint = Color.White)
            }

            // Dot indicators
            if (images.size > 1) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(images.size) { i ->
                        Box(
                            modifier = Modifier
                                .size(if (pagerState.currentPage == i) 18.dp else 6.dp, 6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (pagerState.currentPage == i) Gold
                                    else Color.White.copy(alpha = 0.35f)
                                )
                        )
                    }
                }
            }
        }
    }
}

// ─── Empty state ──────────────────────────────────────────────────────────────
@Composable
fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 90.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("✦", fontSize = 52.sp)
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                "Nothing here yet",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White.copy(alpha = 0.75f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Tap + to capture your first thought",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.3f)
            )
        }
    }
}

// ─── Bottom Sheet ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NoteBottomSheet(
    note: Note?,
    onDismiss: () -> Unit,
    onSave: (String, String, List<String>) -> Unit
) {
    var title by remember { mutableStateOf(note?.title ?: "") }
    var content by remember { mutableStateOf(note?.content ?: "") }
    val images =
        remember { mutableStateListOf<String>().apply { addAll(note?.images ?: emptyList()) } }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val picker =
        rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            uris.forEach { uri ->
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: Exception) {
                }
                val s = uri.toString()
                if (!images.contains(s)) images.add(s)
            }
        }

    ModalBottomSheet(
        onDismissRequest = {
            if (title.isNotBlank() || content.isNotBlank() || images.isNotEmpty())
                onSave(title, content, images.toList())
            else onDismiss()
        },
        sheetState = sheetState,
        containerColor = Color(0xFF0F0F1A),
        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
        modifier = Modifier.imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 22.dp)
                .padding(bottom = 20.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(44.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.18f))
                    .align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = if (note != null) "EDIT NOTE" else "NEW NOTE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Gold.copy(alpha = 0.8f),
                letterSpacing = 3.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = {
                    Text(
                        "Title",
                        fontSize = 26.sp,
                        color = Color.White.copy(alpha = 0.18f),
                        fontWeight = FontWeight.Bold
                    )
                },
                textStyle = TextStyle(
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                placeholder = {
                    Text(
                        "Write your note…",
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.18f)
                    )
                },
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    lineHeight = 24.sp
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                minLines = 4,
                modifier = Modifier.fillMaxWidth()
            )

            AnimatedVisibility(
                visible = images.isNotEmpty(),
                enter = fadeIn(tween(200)) + slideInVertically(tween(200)),
                exit = fadeOut(tween(200)) + slideOutVertically(tween(200))
            ) {
                Column {
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(images) { uri ->
                            Box {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(Uri.parse(uri)).crossfade(true).build(),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(82.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                )
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .align(Alignment.TopEnd)
                                        .background(Color(0xFF0F0F1A), CircleShape)
                                        .combinedClickable(onClick = { images.remove(uri) }),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        null,
                                        tint = Color.White,
                                        modifier = Modifier.size(11.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.White.copy(alpha = 0.07f))
            )
            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Add images
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(13.dp))
                        .background(Cyan.copy(alpha = 0.1f))
                        .combinedClickable(onClick = { picker.launch("image/*") })
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Image,
                            null,
                            tint = Cyan,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(7.dp))
                        Text(
                            "Add Images",
                            color = Cyan,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { scope.launch { sheetState.hide(); onDismiss() } }) {
                        Text("Cancel", color = Color.White.copy(alpha = 0.35f), fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    val canSave = title.isNotBlank() || content.isNotBlank() || images.isNotEmpty()
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(13.dp))
                            .background(
                                if (canSave) Brush.linearGradient(listOf(Gold, Color(0xFFFFA040)))
                                else Brush.linearGradient(
                                    listOf(
                                        Color(0xFF333333),
                                        Color(0xFF333333)
                                    )
                                )
                            )
                            .combinedClickable(
                                enabled = canSave,
                                onClick = {
                                    scope.launch {
                                        sheetState.hide()
                                        onSave(title, content, images.toList())
                                    }
                                }
                            )
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = if (note != null) "Update" else "Save",
                            color = if (canSave) Color(0xFF1A1000) else Color.White.copy(alpha = 0.3f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}