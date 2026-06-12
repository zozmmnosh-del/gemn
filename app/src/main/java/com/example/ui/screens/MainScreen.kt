package com.example.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.data.ProcessedImage
import kotlinx.coroutines.delay
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.BuildConfig
import com.example.ui.viewmodel.WatermarkViewModel
import com.example.utils.SampleImageGenerator
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ScreenState {
    SPLASH,
    HOME,
    EDITOR
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: WatermarkViewModel) {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf(ScreenState.SPLASH) }

    // Observers from ViewModel
    val currentBmp by viewModel.currentBitmap.collectAsState()
    val maskBmp by viewModel.maskBitmap.collectAsState()
    val finalBmp by viewModel.finalBitmap.collectAsState()
    val selectedSample by viewModel.selectedSample.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val isDetecting by viewModel.isDetecting.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()
    val brushSize by viewModel.brushSize.collectAsState()
    val comparisonPosition by viewModel.comparisonPosition.collectAsState()
    val historyList by viewModel.historyList.collectAsState()

    var showAboutDialog by remember { mutableStateOf(false) }
    var zoomScale by remember { mutableStateOf(1f) }

    // Simple single-toast trigger
    LaunchedEffect(userMessage) {
        userMessage?.let {
            viewModel.clearUserMessage()
        }
    }

    // Interactive Photo Picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            viewModel.loadImageUri(context, it)
            currentScreen = ScreenState.EDITOR
        }
    }

    // Modern local File Input / Upload Picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.loadImageUri(context, it)
            currentScreen = ScreenState.EDITOR
        }
    }

    var comparisonMode by remember { mutableStateOf("slider") }

    // Elegant Dark Theme Palette (Material 3 Dark Slate & Lavender)
    val baseBackground = Color(0xFF111318)
    val containerBg1 = Color(0xFF1C1B1F)
    val containerBg2 = Color(0xFF2B2930)
    val borderColor = Color(0xFF49454F)
    val elegantPurple = Color(0xFFD0BCFF)
    val onPurpleText = Color(0xFF381E72)
    val textLight = Color(0xFFE2E2E6)
    val textSubtle = Color(0xFF938F99)

    val universeBg = Brush.verticalGradient(
        colors = listOf(
            baseBackground,
            Color(0xFF16181F),
            baseBackground
        )
    )

    when (currentScreen) {
        ScreenState.SPLASH -> {
            SplashScreen {
                currentScreen = ScreenState.HOME
            }
        }
        ScreenState.HOME -> {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = elegantPurple,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "نقاء جيميناي",
                                    fontFamily = MaterialTheme.typography.titleLarge.fontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = textLight,
                                    fontSize = 22.sp
                                )
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = baseBackground
                        ),
                        actions = {
                            IconButton(onClick = { showAboutDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "المعلومات",
                                    tint = textSubtle
                                )
                            }
                        }
                    )
                }
            ) { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(universeBg)
                        .padding(padding)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Hero Call-to-action Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                                .shadow(8.dp, RoundedCornerShape(32.dp)),
                            shape = RoundedCornerShape(32.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = containerBg1
                            ),
                            border = BorderStroke(1.5.dp, borderColor)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(CircleShape)
                                        .background(elegantPurple.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoLibrary,
                                        contentDescription = null,
                                        tint = elegantPurple,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                                Text(
                                    text = "اختر صورة لتنقية العلامة المائية",
                                    color = textLight,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 16.dp)
                                )
                                Text(
                                    text = "استيراد صورك أو استخدام إحدى النماذج الجاهزة لتجربة إزالة ذكية وسلسة فورا.",
                                    color = textSubtle,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 20.sp,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
                                )

                                Button(
                                    onClick = {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(
                                                ActivityResultContracts.PickVisualMedia.ImageOnly
                                            )
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = elegantPurple,
                                        contentColor = onPurpleText
                                    ),
                                    shape = RoundedCornerShape(24.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(54.dp)
                                        .testTag("gallery_picker_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoLibrary,
                                        contentDescription = null,
                                        tint = onPurpleText
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "استيراد من الاستوديو",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = onPurpleText
                                    )
                                }

                                 Spacer(modifier = Modifier.height(16.dp))

                                 Text(
                                     text = "أو",
                                     color = textSubtle,
                                     fontSize = 12.sp,
                                     fontWeight = FontWeight.Bold,
                                     modifier = Modifier.padding(vertical = 4.dp)
                                 )

                                 Spacer(modifier = Modifier.height(12.dp))

                                 // Fine Dashed local file picker
                                 Box(
                                     modifier = Modifier
                                         .fillMaxWidth()
                                         .height(110.dp)
                                         .clickable { filePickerLauncher.launch("image/*") }
                                         .drawBehind {
                                             val stroke = androidx.compose.ui.graphics.drawscope.Stroke(
                                                 width = 2.dp.toPx(),
                                                 pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                                             )
                                             drawRoundRect(
                                                 color = elegantPurple.copy(alpha = 0.5f),
                                                 style = stroke,
                                                 cornerRadius = androidx.compose.ui.geometry.CornerRadius(24.dp.toPx())
                                             )
                                         }
                                         .background(elegantPurple.copy(alpha = 0.04f), RoundedCornerShape(24.dp))
                                         .padding(16.dp)
                                         .testTag("file_picker_uploader"),
                                     contentAlignment = Alignment.Center
                                 ) {
                                     Row(
                                         verticalAlignment = Alignment.CenterVertically,
                                         horizontalArrangement = Arrangement.Center
                                     ) {
                                         Icon(
                                             imageVector = Icons.Default.Upload,
                                             contentDescription = null,
                                             tint = elegantPurple,
                                             modifier = Modifier.size(32.dp)
                                         )
                                         Spacer(modifier = Modifier.width(16.dp))
                                         Column {
                                             Text(
                                                 text = "تحميل ملف صورة محلي",
                                                 color = textLight,
                                                 fontWeight = FontWeight.Bold,
                                                 fontSize = 14.sp
                                             )
                                             Spacer(modifier = Modifier.height(4.dp))
                                             Text(
                                                 text = "انقر للتصفح واختيار ملف بدقة كاملة",
                                                 color = textSubtle,
                                                 fontSize = 11.sp
                                             )
                                         }
                                     }
                                 }
                             }
                         }

                        // programmatic samples container
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "نماذج جيميناي الجاهزة للتجربة",
                                color = textLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Right
                            )
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = elegantPurple,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        val samples = remember { SampleImageGenerator.getSamples() }
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(samples) { sample ->
                                SampleImageItem(sample = sample) {
                                    viewModel.loadSample(sample)
                                    currentScreen = ScreenState.EDITOR
                                }
                            }
                        }

                        // Process History Database section
                        Spacer(modifier = Modifier.height(28.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "السجل والأرشيف المحلي",
                                color = textLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            if (historyList.isNotEmpty()) {
                                TextButton(
                                    onClick = { viewModel.clearHistory() },
                                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red.copy(alpha = 0.8f))
                                ) {
                                    Text("مسح الكل", fontSize = 13.sp)
                                }
                            }
                        }

                        if (historyList.isEmpty()) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = containerBg1
                                ),
                                shape = RoundedCornerShape(24.dp),
                                border = BorderStroke(1.dp, borderColor)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = null,
                                        tint = textSubtle,
                                        modifier = Modifier.size(44.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "الأرشيف فارغ حالياً",
                                        color = textSubtle,
                                        fontSize = 13.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.height(10.dp))
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 400.dp)
                            ) {
                                items(historyList) { historyItem ->
                                    HistoryGridItem(
                                        item = historyItem,
                                        onDelete = { viewModel.deleteHistoryItem(historyItem) }
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }
            }
        }
        ScreenState.EDITOR -> {
            Scaffold(
                topBar = {
                    OptIn(ExperimentalMaterial3Api::class)
                    TopAppBar(
                        title = {
                            Text(
                                text = if (selectedSample != null) selectedSample!!.nameAr else "محرر التنقية الذكي",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = textLight
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                viewModel.resetEditorState()
                                currentScreen = ScreenState.HOME
                            }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "الرجوع",
                                    tint = textLight
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = baseBackground
                        ),
                        actions = {
                            if (finalBmp == null && currentBmp != null) {
                                IconButton(
                                    onClick = { viewModel.clearMask() },
                                    modifier = Modifier.testTag("clear_mask_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "مسح التلوين",
                                        tint = textLight
                                    )
                                }
                            }
                        }
                    )
                }
            ) { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(universeBg)
                        .padding(padding)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Main Editor Area (Image / Canvas / Before-After Slider)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (finalBmp != null && currentBmp != null) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    // Row of chips for comparison mode
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    ) {
                                        listOf(
                                            "slider" to "المقارنة التفاعلية",
                                            "side_by_side" to "جنباً إلى جنب",
                                            "toggle" to "التبديل السريع"
                                        ).forEach { (mode, title) ->
                                            val isSelected = comparisonMode == mode
                                            Box(
                                                modifier = Modifier
                                                    .background(
                                                        color = if (isSelected) elegantPurple else containerBg1,
                                                        shape = RoundedCornerShape(20.dp)
                                                    )
                                                    .clickable { comparisonMode = mode }
                                                    .border(
                                                        width = 1.dp,
                                                        color = if (isSelected) elegantPurple else borderColor,
                                                        shape = RoundedCornerShape(20.dp)
                                                    )
                                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                                            ) {
                                                Text(
                                                    text = title,
                                                    color = if (isSelected) onPurpleText else textLight,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    Box(
                                        modifier = Modifier.weight(1f).fillMaxWidth(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (comparisonMode == "slider") {
                                            CompareSlider(
                                                before = currentBmp!!,
                                                after = finalBmp!!,
                                                position = comparisonPosition,
                                                onPositionChange = { viewModel.setComparisonPosition(it) }
                                            )
                                        } else if (comparisonMode == "side_by_side") {
                                            Row(
                                                modifier = Modifier.fillMaxSize(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier.weight(1f).fillMaxHeight(),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Image(
                                                        bitmap = currentBmp!!.asImageBitmap(),
                                                        contentDescription = "الأصلية",
                                                        modifier = Modifier.clip(RoundedCornerShape(12.dp)).fillMaxSize(),
                                                        contentScale = ContentScale.Fit
                                                    )
                                                    Box(
                                                        modifier = Modifier
                                                            .align(Alignment.TopStart)
                                                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(topStart = 12.dp, bottomEnd = 12.dp))
                                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                                    ) {
                                                        Text("الأصلية", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                                Box(
                                                    modifier = Modifier.weight(1f).fillMaxHeight(),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Image(
                                                        bitmap = finalBmp!!.asImageBitmap(),
                                                        contentDescription = "المنقاة",
                                                        modifier = Modifier.clip(RoundedCornerShape(12.dp)).fillMaxSize(),
                                                        contentScale = ContentScale.Fit
                                                    )
                                                    Box(
                                                        modifier = Modifier
                                                            .align(Alignment.TopStart)
                                                            .background(elegantPurple, RoundedCornerShape(topStart = 12.dp, bottomEnd = 12.dp))
                                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                                    ) {
                                                        Text("المنقاة", color = onPurpleText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        } else {
                                            var showPurified by remember { mutableStateOf(true) }
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clickable { showPurified = !showPurified },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Image(
                                                    bitmap = if (showPurified) finalBmp!!.asImageBitmap() else currentBmp!!.asImageBitmap(),
                                                    contentDescription = "مقارنة سريعة",
                                                    modifier = Modifier.clip(RoundedCornerShape(16.dp)).fillMaxSize(),
                                                    contentScale = ContentScale.Fit
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .align(Alignment.BottomCenter)
                                                        .padding(bottom = 12.dp)
                                                        .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(14.dp))
                                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                                ) {
                                                    Text(
                                                        text = if (showPurified) "النسخة المنقاة (اضغط للمقارنة)" else "النسخة الأصلية (اضغط للعودة)",
                                                        color = Color.White,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            } else if (currentBmp != null) {
                                // Manual Paint Canvas Drawer Workspace
                                PaintWorkspace(
                                    image = currentBmp!!,
                                    mask = maskBmp,
                                    onDraw = { x, y ->
                                        viewModel.drawScribbleOnMask(x, y)
                                    }
                                )
                            }
                        }

                        // Floating / Action Tools Controls
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (finalBmp != null) {
                                // RESULTS OPTIONS MODULE
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = containerBg1
                                    ),
                                    shape = RoundedCornerShape(24.dp),
                                    border = BorderStroke(1.dp, borderColor)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "تمت إزالة العلامة المائية بنجاح!",
                                            color = textLight,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            text = "اختر طريقة العرض المناسبة لتجربة جودة الإزالة فائقة الدقة.",
                                            color = textSubtle,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedButton(
                                                onClick = {
                                                    viewModel.resetEditorState()
                                                    currentScreen = ScreenState.HOME
                                                },
                                                border = BorderStroke(1.dp, borderColor),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = textLight),
                                                shape = RoundedCornerShape(24.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(Icons.Default.ArrowBack, contentDescription = null, tint = textLight)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("الرئيسية", color = textLight, fontSize = 11.sp)
                                            }

                                            Button(
                                                onClick = {
                                                    viewModel.savePurifiedImageToHistory(context)
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = containerBg2,
                                                    contentColor = textLight
                                                ),
                                                border = BorderStroke(1.dp, borderColor),
                                                shape = RoundedCornerShape(24.dp),
                                                modifier = Modifier.weight(1.2f).testTag("save_purified_button")
                                            ) {
                                                Icon(
                                                    Icons.Default.Save,
                                                    contentDescription = null,
                                                    tint = textLight
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("حفظ بالأرشيف", color = textLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }

                                            Button(
                                                onClick = {
                                                    viewModel.downloadImageToPublicDownloads(context)
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = elegantPurple,
                                                    contentColor = onPurpleText
                                                ),
                                                shape = RoundedCornerShape(24.dp),
                                                modifier = Modifier.weight(1.5f).testTag("download_purified_button")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Download,
                                                    contentDescription = null,
                                                    tint = onPurpleText
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("تصدير للجهاز", color = onPurpleText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            } else {
                                // EDITING WORKSPACE OPTIONS TOOLS MODULE
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = containerBg2
                                    ),
                                    shape = RoundedCornerShape(24.dp),
                                    border = BorderStroke(1.dp, borderColor)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp)
                                    ) {
                                        // Slider to adjust Brush size
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Brush,
                                                contentDescription = null,
                                                tint = textLight,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "حجم فرشاة التلوين اليدوي",
                                                color = textLight,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.weight(1f))
                                            Text(
                                                text = "${brushSize.toInt()} px",
                                                color = elegantPurple,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Slider(
                                            value = brushSize,
                                            onValueChange = { viewModel.setBrushSize(it) },
                                            valueRange = 8f..100f,
                                            colors = SliderDefaults.colors(
                                                activeTrackColor = elegantPurple,
                                                thumbColor = Color.White,
                                                inactiveTrackColor = borderColor
                                            ),
                                            modifier = Modifier.fillMaxWidth().testTag("brush_size_slider")
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))
                                        
                                        // Auto-Detect & Healing action buttons
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            OutlinedButton(
                                                onClick = { viewModel.detectWatermarkWithAI() },
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = elegantPurple),
                                                border = BorderStroke(1.2.dp, elegantPurple),
                                                shape = RoundedCornerShape(24.dp),
                                                enabled = !isDetecting,
                                                modifier = Modifier.weight(1f).height(48.dp).testTag("ai_detection_button")
                                            ) {
                                                if (isDetecting) {
                                                    CircularProgressIndicator(
                                                        color = elegantPurple,
                                                        modifier = Modifier.size(20.dp),
                                                        strokeWidth = 2.dp
                                                    )
                                                } else {
                                                    Icon(
                                                        imageVector = Icons.Default.AutoAwesome,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(16.dp),
                                                        tint = elegantPurple
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "رصد بالذكاء الاصطناعي",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = elegantPurple
                                                    )
                                                }
                                            }

                                            Button(
                                                onClick = { viewModel.removeWatermark(context) },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = elegantPurple,
                                                    contentColor = onPurpleText
                                                ),
                                                shape = RoundedCornerShape(24.dp),
                                                modifier = Modifier.weight(1f).height(48.dp).testTag("inpaint_process_button")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.AutoAwesome,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp),
                                                    tint = onPurpleText
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "إزالة بلمسة ذكية",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp,
                                                    color = onPurpleText
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dynamic processing loading animation overlays
    if (isProcessing || isDetecting) {
        Dialog(onDismissRequest = {}) {
            Card(
                colors = CardDefaults.cardColors(containerColor = containerBg1),
                shape = RoundedCornerShape(32.dp),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .border(2.dp, elegantPurple, RoundedCornerShape(32.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val infiniteTransition = rememberInfiniteTransition(label = "")
                    val angle by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1200, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "loading"
                    )

                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = elegantPurple,
                        modifier = Modifier
                            .size(52.dp)
                            .pointerInput(Unit) {}
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = if (isProcessing) "جاري المعالجة الرقمية" else "تحليل صورة جيميناي",
                        color = textLight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Text(
                        text = statusMessage,
                        color = textSubtle,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    LinearProgressIndicator(
                        color = elegantPurple,
                        trackColor = borderColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                    )
                }
            }
        }
    }

    // About Information Dialog in Arabic
    if (showAboutDialog) {
        Dialog(onDismissRequest = { showAboutDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = containerBg1),
                shape = RoundedCornerShape(32.dp),
                border = BorderStroke(1.5.dp, elegantPurple)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "عن تطبيق نقاء جيميناي",
                        color = textLight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Text(
                        text = "تم تطوير هذا التطبيق كأداة متكاملة مخصصة لتنقية وإزالة العلامات المائية والشعارات الرقمية من صور جيميناي أو أي صور ذكاء اصطناعي أخرى بأسلوب احترافي.\n\n" +
                               "يتضمن التطبيق عنصرين جوهريين:\n" +
                               "1️⃣ كشف ذكي بالذكاء الاصطناعي (Gemini 3.5 Flash) لتحديد وإحاطة العلامات المائية بشكل تلقائي ومؤتمت بالكامل.\n" +
                               "2️⃣ محرك علاج بيكسلات متقدم (Advanced Pixel Micro-Diffusion Engine) لحذف المنطقة المشوهة ونشر ألوان الخلفية المجاورة لتبدو طبيعية وبدون غباش في الأنسجة.\n\n" +
                               "التطبيق يعمل محلياً بالكامل للمعالجة الفورية، مع حفظ سجل أعمالك كأرشيف دائم.",
                        color = textSubtle,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Right,
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    Button(
                        onClick = { showAboutDialog = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = elegantPurple,
                            contentColor = onPurpleText
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("فهمت", color = onPurpleText, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // User Toast notification dialog block
    var triggerMessage by remember(userMessage) { mutableStateOf(userMessage) }
    if (triggerMessage != null) {
        Dialog(onDismissRequest = { triggerMessage = null }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = containerBg1),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.5.dp, elegantPurple)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = elegantPurple,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "تنبيه النظام الذكي",
                            color = textLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = triggerMessage!!,
                        color = textSubtle,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Button(
                        onClick = {
                            triggerMessage = null
                            viewModel.clearUserMessage()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = elegantPurple,
                            contentColor = onPurpleText
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("موافق", color = onPurpleText, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Animated Splash Screen following creative visual atmospheric guidelines
 */
@Composable
fun SplashScreen(onComplete: () -> Unit) {
    val scale = remember { Animatable(0.5f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        alpha.animateTo(1f, animationSpec = tween(800))
        delay(1500) // Delay to showcase splash beautiful glow
        onComplete()
    }

    val elegantPurple = Color(0xFFD0BCFF)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF111318)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .shadow(16.dp, CircleShape, clip = false)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                elegantPurple.copy(alpha = 0.25f),
                                Color.Transparent
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = elegantPurple,
                    modifier = Modifier
                        .size(64.dp)
                        .scale(scale.value)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "GEMINI PURE",
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp,
                fontSize = 24.sp,
                color = Color(0xFFE2E2E6),
                textAlign = TextAlign.Center,
                modifier = Modifier.scale(scale.value)
            )

            Text(
                text = "نقاء جيميناي",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = elegantPurple,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp).scale(scale.value)
            )

            Spacer(modifier = Modifier.height(10.dp))
            
            Text(
                text = "الحذف الذكي للعلامات المائية والأختام الاصطناعية",
                fontSize = 12.sp,
                color = Color(0xFF938F99),
                textAlign = TextAlign.Center,
                modifier = Modifier.scale(scale.value)
            )
        }
    }
}

@Composable
fun SampleImageItem(
    sample: SampleImageGenerator.SampleImage,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(260.dp)
            .height(280.dp)
            .clickable { onClick() }
            .shadow(4.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1B1F)),
        border = BorderStroke(1.dp, Color(0xFF49454F))
    ) {
        Column {
            Image(
                bitmap = sample.bitmap.asImageBitmap(),
                contentDescription = sample.nameEn,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = sample.nameAr,
                        color = Color(0xFFE2E2E6),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = sample.description,
                        color = Color(0xFF938F99),
                        fontSize = 11.sp,
                        maxLines = 2,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF2B2930), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = Color(0xFFD0BCFF),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "علامة: ${sample.watermarkType.substringBefore(" (")}",
                        color = Color(0xFFD0BCFF),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryGridItem(
    item: ProcessedImage,
    onDelete: () -> Unit
) {
    val file = File(item.cleanedPath)
    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Load scaled thumbnail asynchronously
    LaunchedEffect(item.cleanedPath) {
        if (file.exists()) {
            val loaded = BitmapFactory.decodeFile(item.cleanedPath)
            imageBitmap = loaded
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .shadow(4.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1B1F)),
        border = BorderStroke(1.dp, Color(0xFF49454F))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (imageBitmap != null) {
                Image(
                    bitmap = imageBitmap!!.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White.copy(alpha = 0.05f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFFD0BCFF), modifier = Modifier.size(24.dp))
                }
            }

            // Info & actions card overlays
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)),
                            startY = 100f
                        )
                    )
            )

            // Delete float overlay
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(32.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "حذف",
                    tint = Color.Red,
                    modifier = Modifier.size(16.dp)
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                Text(
                    text = item.imageName,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    maxLines = 1
                )
                Text(
                    text = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(item.timestamp)),
                    color = Color.LightGray,
                    fontSize = 9.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

/**
 * Interactive workspace holding the brush gesture paint-over canvas
 */
@Composable
fun PaintWorkspace(
    image: Bitmap,
    mask: Bitmap?,
    onDraw: (Float, Float) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
            .shadow(12.dp, RoundedCornerShape(32.dp))
            .background(Color(0xFF1C1B1F), RoundedCornerShape(32.dp))
            .border(2.dp, Color(0xFF49454F), RoundedCornerShape(32.dp)),
        contentAlignment = Alignment.Center
    ) {
        val aspectImage = image.width.toFloat() / image.height.toFloat()
        val aspectLayout = maxWidth.value / maxHeight.value

        val (drawWidth, drawHeight) = if (aspectImage > aspectLayout) {
            Pair(maxWidth, maxWidth / aspectImage)
        } else {
            Pair(maxHeight * aspectImage, maxHeight)
        }

        Box(
            modifier = Modifier
                .size(width = drawWidth, height = drawHeight)
                .clip(RoundedCornerShape(8.dp))
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        val normX = change.position.x / size.width.toFloat()
                        val normY = change.position.y / size.height.toFloat()
                        onDraw(normX.coerceIn(0f, 1f), normY.coerceIn(0f, 1f))
                    }
                }
        ) {
            // Layer 1: Base original image
            Image(
                bitmap = image.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize()
            )

            // Layer 2: Semi-transparent mask overlay
            mask?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.fillMaxSize(),
                    alpha = 0.55f // Beautiful translucent blend to clearly view behind layers while painting
                )
            }
        }
    }
}

/**
 * Extremely polished Drag-to-Compare slider split screen
 */
@Composable
fun CompareSlider(
    before: Bitmap,
    after: Bitmap,
    position: Float,
    onPositionChange: (Float) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
            .shadow(12.dp, RoundedCornerShape(32.dp))
            .background(Color(0xFF1C1B1F), RoundedCornerShape(32.dp))
            .border(2.dp, Color(0xFF49454F), RoundedCornerShape(32.dp)),
        contentAlignment = Alignment.Center
    ) {
        val aspectImage = before.width.toFloat() / before.height.toFloat()
        val aspectLayout = maxWidth.value / maxHeight.value

        val (drawWidth, drawHeight) = if (aspectImage > aspectLayout) {
            Pair(maxWidth, maxWidth / aspectImage)
        } else {
            Pair(maxHeight * aspectImage, maxHeight)
        }

        Box(
            modifier = Modifier
                .size(width = drawWidth, height = drawHeight)
                .clip(RoundedCornerShape(8.dp))
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        onPositionChange(change.position.x / size.width.toFloat())
                    }
                }
        ) {
            // Layer 1: After image (Complete full-screen)
            Image(
                bitmap = after.asImageBitmap(),
                contentDescription = "بعد التنقية",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize()
            )

            // Layer 2: Before image (Clipped in width according to position)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(position)
                    .clipToBounds()
            ) {
                Image(
                    bitmap = before.asImageBitmap(),
                    contentDescription = "قبل التنقية",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.requiredSize(width = drawWidth, height = drawHeight)
                )
            }

            // Vertical neon line separator
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(3.dp)
                    .align(Alignment.CenterStart)
                    .offset(x = drawWidth * position)
                    .background(Color(0xFFD0BCFF))
            )

            // Elegant Drag Handle Button
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = (drawWidth * position) - 20.dp)
                    .size(40.dp)
                    .shadow(6.dp, CircleShape)
                    .background(Color(0xFFD0BCFF), CircleShape)
                    .border(2.dp, Color(0xFF2B2930), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Compare,
                    contentDescription = null,
                    tint = Color(0xFF381E72),
                    modifier = Modifier.size(20.dp)
                )
            }
            
            // Text Badges
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .background(Color(0xFF2B2930).copy(alpha = 0.85f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("المطورة", color = Color(0xFFD0BCFF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(Color(0xFF2B2930).copy(alpha = 0.85f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("المنقاة", color = Color(0xFFD0BCFF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
