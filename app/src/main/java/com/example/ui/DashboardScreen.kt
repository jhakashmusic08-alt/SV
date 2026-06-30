package com.example.ui

import android.app.DatePickerDialog
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.graphics.pdf.PdfDocument
import android.graphics.Paint as AndroidPaint
import android.graphics.Typeface
import android.os.Environment
import android.content.ContentValues
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream
import com.example.R
import com.example.data.Release
import com.example.data.RecentActivity
import com.example.ui.theme.*
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: ReleaseViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf(0) } // 0: Hub, 1: Distribute, 2: Stores & Savings

    val releases by viewModel.uiState.collectAsStateWithLifecycle()
    val artistName by viewModel.artistName.collectAsStateWithLifecycle()
    val recordLabel by viewModel.recordLabel.collectAsStateWithLifecycle()
    val artistBio by viewModel.artistBio.collectAsStateWithLifecycle()
    val spotifyLink by viewModel.spotifyLink.collectAsStateWithLifecycle()
    val appleMusicLink by viewModel.appleMusicLink.collectAsStateWithLifecycle()
    val instagramLink by viewModel.instagramLink.collectAsStateWithLifecycle()
    val youtubeLink by viewModel.youtubeLink.collectAsStateWithLifecycle()
    val artistImageType by viewModel.artistImageType.collectAsStateWithLifecycle()
    val totalStreams by viewModel.totalStreams.collectAsStateWithLifecycle()
    val totalEarnings by viewModel.totalEarnings.collectAsStateWithLifecycle()
    val newsletterEmail by viewModel.newsletterEmail.collectAsStateWithLifecycle()
    val isSubscribed by viewModel.isSubscribed.collectAsStateWithLifecycle()
    val searchUiState by viewModel.searchUiState.collectAsStateWithLifecycle()
    val emailNotificationsEnabled by viewModel.emailNotificationsEnabled.collectAsStateWithLifecycle()
    val onboardingCompleted by viewModel.onboardingCompleted.collectAsStateWithLifecycle()
    val recentActivities by viewModel.recentActivities.collectAsStateWithLifecycle()

    var showProfileDialog by remember { mutableStateOf(false) }
    var onboardingStep by remember { mutableStateOf(-1) }

    LaunchedEffect(onboardingCompleted) {
        if (!onboardingCompleted && onboardingStep == -1) {
            onboardingStep = 0
        }
    }

    LaunchedEffect(onboardingStep) {
        when (onboardingStep) {
            0 -> currentTab = 0
            1 -> currentTab = 0
            2 -> currentTab = 1
            3 -> currentTab = 2
            4 -> currentTab = 0
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = SpaceDark,
        bottomBar = {
            NavigationBar(
                containerColor = CarbonSlate,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .shadow(16.dp, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .border(1.dp, Color(0xFFCAC4D0), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            ) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Hub") },
                    label = { Text("Hub") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SpaceDark,
                        selectedTextColor = NeonTeal,
                        indicatorColor = NeonTeal,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_hub")
                )
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    icon = { Icon(Icons.Default.CloudUpload, contentDescription = "Distribute") },
                    label = { Text("Distribute") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SpaceDark,
                        selectedTextColor = NeonTeal,
                        indicatorColor = NeonTeal,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_distribute")
                )
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 },
                    icon = { Icon(Icons.Default.BarChart, contentDescription = "Analytics") },
                    label = { Text("Analytics") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SpaceDark,
                        selectedTextColor = NeonTeal,
                        indicatorColor = NeonTeal,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_analytics")
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> -width } + fadeOut())
                    } else {
                        (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> width } + fadeOut())
                    }
                },
                label = "tab_transition"
            ) { tab ->
                when (tab) {
                    0 -> HubTab(
                        releases = releases,
                        recentActivities = recentActivities,
                        artistName = artistName,
                        recordLabel = recordLabel,
                        artistBio = artistBio,
                        spotifyLink = spotifyLink,
                        appleMusicLink = appleMusicLink,
                        instagramLink = instagramLink,
                        youtubeLink = youtubeLink,
                        artistImageType = artistImageType,
                        totalStreams = totalStreams,
                        totalEarnings = totalEarnings,
                        newsletterEmail = newsletterEmail,
                        isSubscribed = isSubscribed,
                        searchUiState = searchUiState,
                        onSubscribeNewsletter = { email -> viewModel.subscribeNewsletter(email) },
                        onUnsubscribeNewsletter = { viewModel.unsubscribeNewsletter() },
                        onSearchTrends = { query -> viewModel.performTrendSearch(query) },
                        onEditProfile = { showProfileDialog = true },
                        onDeleteRelease = { viewModel.deleteRelease(it) },
                        onStartWalkthrough = { onboardingStep = 0 }
                    )
                    1 -> DistributeTab(
                        artistName = artistName,
                        recordLabel = recordLabel,
                        onDistribute = { title, artist, featuring, lyricist, composer, genre, lang, date, label ->
                            viewModel.createRelease(title, artist, featuring, lyricist, composer, genre, lang, date, label)
                            currentTab = 0
                            Toast.makeText(context, "Song Submitted! Tracking live on your Hub.", Toast.LENGTH_LONG).show()
                        }
                    )
                    2 -> StoresTab(artistName = artistName, recordLabel = recordLabel)
                }
            }

            if (showProfileDialog) {
                ProfileDialog(
                    initialArtist = artistName,
                    initialLabel = recordLabel,
                    initialBio = artistBio,
                    initialSpotify = spotifyLink,
                    initialApple = appleMusicLink,
                    initialInstagram = instagramLink,
                    initialYoutube = youtubeLink,
                    initialImageType = artistImageType,
                    initialEmailNotifications = emailNotificationsEnabled,
                    onDismiss = { showProfileDialog = false },
                    onSave = { name, label, bio, spotify, apple, instagram, youtube, imageType, emailNotifications ->
                        viewModel.updateProfile(name, label, bio, spotify, apple, instagram, youtube, imageType, emailNotifications)
                        showProfileDialog = false
                        Toast.makeText(context, "Artist Profile Updated!", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            if (onboardingStep >= 0) {
                OnboardingWalkthroughOverlay(
                    step = onboardingStep,
                    onNext = {
                        if (onboardingStep < 4) {
                            onboardingStep++
                        } else {
                            viewModel.setOnboardingCompleted(true)
                            onboardingStep = -1
                        }
                    },
                    onBack = {
                        if (onboardingStep > 0) {
                            onboardingStep--
                        }
                    },
                    onSkip = {
                        viewModel.setOnboardingCompleted(true)
                        onboardingStep = -1
                    }
                )
            }
        }
    }
}

// ==========================================
// TAB 1: ARTIST HUB (DASHBOARD)
// ==========================================
@Composable
fun HubTab(
    releases: List<Release>,
    artistName: String,
    recordLabel: String,
    artistBio: String,
    spotifyLink: String,
    appleMusicLink: String,
    instagramLink: String,
    youtubeLink: String,
    artistImageType: String,
    totalStreams: Long,
    totalEarnings: Double,
    newsletterEmail: String,
    isSubscribed: Boolean,
    searchUiState: SearchUiState,
    onSubscribeNewsletter: (String) -> Unit,
    onUnsubscribeNewsletter: () -> Unit,
    onSearchTrends: (String) -> Unit,
    onEditProfile: () -> Unit,
    onDeleteRelease: (Release) -> Unit,
    onStartWalkthrough: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Sleek Interface Top Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(NeonViolet, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.RocketLaunch,
                            contentDescription = "Rocket Launch",
                            tint = NeonPink,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "ST Free Panel",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Always Free Digital Growth",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Quick Tour Pill Button
                    Row(
                        modifier = Modifier
                            .background(NeonTeal.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                            .border(1.dp, NeonTeal.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .clickable { onStartWalkthrough() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("tour_button"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Tour Guide",
                            tint = NeonTeal,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Tour Guide",
                            color = NeonTeal,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(CarbonLight, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = NeonPink,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(2.dp)) }

        // Hero 3D Banner Image styled with Sleek Interface gradient
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(NeonViolet, NeonTeal)
                        )
                    )
                    .border(1.dp, GlassBorder, RoundedCornerShape(32.dp))
            ) {
                // Ambient soft decorative circles as requested in HTML
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 20.dp, y = (-20).dp)
                        .background(Color.White.copy(alpha = 0.15f), CircleShape)
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "100% FREE FOREVER",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Sab Kuchh Bilkul Free hai!",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        fontFamily = FontFamily.SansSerif
                    )
                    Text(
                        text = "Zero Cost. No charges, no fees, no subscriptions. Keep 100% royalties!",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Artist Profile Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CarbonSlate, RoundedCornerShape(20.dp))
                    .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ArtistProfileImage(imageType = artistImageType, modifier = Modifier.size(56.dp))

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1.0f)) {
                    Text(
                        text = artistName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Label: $recordLabel",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                IconButton(
                    onClick = onEditProfile,
                    modifier = Modifier
                        .background(CarbonLight, CircleShape)
                        .size(40.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit Profile",
                        tint = NeonTeal,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Biography and Social Accounts Card
        item {
            var isExpanded by remember { mutableStateOf(false) }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
                    .testTag("artist_profile_bio_card"),
                colors = CardDefaults.cardColors(containerColor = CarbonSlate),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, GlassBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = NeonTeal, modifier = Modifier.size(16.dp))
                            Text(
                                text = "Artist Bio & Social Identity",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        IconButton(
                            onClick = { isExpanded = !isExpanded },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Toggle",
                                tint = NeonTeal,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isExpanded) artistBio else artistBio.take(80) + if (artistBio.length > 80) "..." else "",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )

                    if (isExpanded) {
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = CarbonLight, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Linked Platform Accounts for Metadata Ingestion",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonPink,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        SocialLinkStatusRow(
                            platformName = "Spotify Artist URL",
                            link = spotifyLink,
                            icon = Icons.Default.MusicNote,
                            color = Color(0xFF1DB954)
                        )
                        SocialLinkStatusRow(
                            platformName = "Apple Music Artist URL",
                            link = appleMusicLink,
                            icon = Icons.Default.Audiotrack,
                            color = Color(0xFFFC3C44)
                        )
                        SocialLinkStatusRow(
                            platformName = "Instagram Handle",
                            link = instagramLink,
                            icon = Icons.Default.PhotoCamera,
                            color = Color(0xFFE1306C)
                        )
                        SocialLinkStatusRow(
                            platformName = "YouTube Channel",
                            link = youtubeLink,
                            icon = Icons.Default.PlayArrow,
                            color = Color(0xFFFF0000)
                        )
                    }
                }
            }
        }

        // Analytics 3D Glass Cards (Row)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Streams Card
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(CarbonSlate, RoundedCornerShape(20.dp))
                        .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = NeonTeal, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Streams", color = TextSecondary, fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = String.format("%,d", totalStreams),
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text("+12.4% today", color = SuccessGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                // Earnings Card
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(CarbonSlate, RoundedCornerShape(20.dp))
                        .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CurrencyRupee, contentDescription = null, tint = NeonPink, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("Earnings", color = TextSecondary, fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = String.format("₹%.2f", totalEarnings),
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text("100% Kept", color = NeonTeal, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Feature highlights: Free Stores Slider Info
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(listOf(CarbonSlate, CarbonLight)),
                        RoundedCornerShape(20.dp)
                    )
                    .border(1.dp, GlassBorder.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "Free Digital Distribution Benefits",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonTeal
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    BenefitChip(icon = Icons.Default.CheckCircle, text = "Keep 100% Royalties")
                    BenefitChip(icon = Icons.Default.Cloud, text = "Free Spotify & Apple")
                    BenefitChip(icon = Icons.Default.TrendingUp, text = "Free ISRC/UPC")
                }
            }
        }

        // Sleek Interface Daily Bonus Card
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CarbonSlate, RoundedCornerShape(24.dp))
                    .border(1.dp, Color(0xFFCAC4D0), RoundedCornerShape(24.dp))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(NeonPink, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "FREE",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Column {
                        Text(
                            text = "Lifetime Free Account Unlocked",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Unlimited free songs, ISRC, and distribution. No charges ever!",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Claim Bonus",
                    tint = NeonTeal,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // How It Works Section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CarbonSlate, RoundedCornerShape(24.dp))
                    .border(1.dp, Color(0xFFCAC4D0), RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                Text(
                    text = "How It Works • Muft & Easy",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonTeal
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "No subscription, no dynamic fees. Keep 100% of your earnings forever.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Step 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(CarbonLight, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = null,
                            tint = NeonTeal,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "1. Upload Your Songs",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Fill the metadata (song name, artist, label) and upload your tracks and cover photo instantly.",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Step 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(CarbonLight, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = NeonTeal,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "2. Auto-Review & ISRC",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Our systems automatically validate your upload and generate free ISRC & UPC codes within minutes.",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Step 3
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(CarbonLight, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            tint = NeonTeal,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "3. Global Free Distribution",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Your music gets delivered straight to Spotify, Apple Music, YouTube Music, JioSaavn, Wynk, and 150+ more platforms.",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Step 4
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(NeonPink, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "4. Keep 100% of Your Royalties",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonTeal
                        )
                        Text(
                            text = "Every single rupee from streams and downloads belongs to you. Zero cuts, zero commissions, zero surprise fees.",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        // Active Releases Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Music Releases (${releases.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Live Statuses",
                    fontSize = 12.sp,
                    color = NeonTeal
                )
            }
        }

        // Release list
        if (releases.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.CloudQueue,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "No releases yet",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Click 'Distribute' to launch your first free track!",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        } else {
            items(releases, key = { it.id }) { release ->
                ReleaseItemCard(release = release, onDelete = { onDeleteRelease(release) })
            }
        }

        item {
            GoogleMusicTrendsSearchWidget(
                searchUiState = searchUiState,
                onSearch = onSearchTrends
            )
        }

        item {
            NewsletterSubscriptionFooter(
                isSubscribed = isSubscribed,
                subscribedEmail = newsletterEmail,
                onSubscribe = onSubscribeNewsletter,
                onUnsubscribe = onUnsubscribeNewsletter
            )
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
fun BenefitChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(SpaceDark.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Icon(icon, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(12.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, color = TextPrimary, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun ReleaseItemCard(release: Release, onDelete: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    val statusColor = when (release.status) {
        "Live in Stores" -> SuccessGreen
        "Delivered to Stores" -> NeonTeal
        "Approved" -> NeonViolet
        else -> AlertOrange
    }

    val progressValue = when (release.status) {
        "Live in Stores" -> 1.0f
        "Delivered to Stores" -> 0.75f
        "Approved" -> 0.45f
        else -> 0.15f
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progressValue,
        animationSpec = spring(stiffness = Spring.StiffnessLow)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CarbonSlate, RoundedCornerShape(18.dp))
            .border(1.dp, GlassBorder.copy(alpha = if (expanded) 0.6f else 0.2f), RoundedCornerShape(18.dp))
            .clickable { expanded = !expanded }
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Album art 3D mock
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(listOf(NeonTeal, NeonViolet)))
                    .padding(1.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(11.dp))
                        .background(CarbonLight)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_app_icon_1782777870303),
                        contentDescription = "Vinyl",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        colorFilter = ColorFilter.tint(NeonTeal.copy(alpha = 0.15f))
                    )
                    Icon(
                        Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = NeonTeal,
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.Center)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = release.title,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${release.artistName} • ${release.genre}",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(statusColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = release.status,
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            IconButton(onClick = { expanded = !expanded }) {
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Details",
                    tint = TextSecondary
                )
            }
        }

        // Live Tracker Timeline / Progress
        Spacer(modifier = Modifier.height(10.dp))
        LinearProgressIndicator(
            progress = animatedProgress,
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(CircleShape),
            color = statusColor,
            trackColor = CarbonLight
        )

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .background(SpaceDark.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Text("Release Information", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeonTeal)
                Spacer(modifier = Modifier.height(8.dp))

                InfoRow(label = "Language", value = release.language)
                InfoRow(label = "Record Label", value = release.recordLabel)
                InfoRow(label = "Release Date", value = release.releaseDate)
                InfoRow(label = "UPC Code", value = release.upcCode.ifEmpty { "Generating..." })
                InfoRow(label = "ISRC Code", value = release.isrcCode.ifEmpty { "Generating..." })

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = CarbonLight, thickness = 1.dp)
                Spacer(modifier = Modifier.height(10.dp))

                StoreDistributionTracker(release = release)

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = CarbonLight, thickness = 1.dp)
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Status: ${release.status}",
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Button(
                        onClick = onDelete,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.2f), contentColor = Color.Red),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("Takedown", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun StoreDistributionTracker(release: Release) {
    var isCheckingStatus by remember { mutableStateOf(false) }
    var selectedStoreForLogs by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    val (overallProgress, statusText) = when (release.status) {
        "Live in Stores" -> 1.0f to "Fully Live on 150+ Streaming Platforms"
        "Delivered to Stores" -> 0.75f to "Delivered to Stores • Indexing Files"
        "Approved" -> 0.45f to "Approved by QC • Outbound Delivery Queued"
        else -> 0.15f to "In Review Queue • Metadata Verification Active"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Global Stores Distribution Tracker",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = NeonTeal
            )
            
            TextButton(
                onClick = {
                    isCheckingStatus = true
                    val timer = Timer()
                    timer.schedule(object : TimerTask() {
                        override fun run() {
                            isCheckingStatus = false
                            timer.cancel()
                        }
                    }, 1200)
                },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier.height(24.dp)
            ) {
                if (isCheckingStatus) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(10.dp),
                        strokeWidth = 1.5.dp,
                        color = NeonTeal
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Pinging API...", fontSize = 9.sp, color = NeonTeal)
                } else {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = NeonTeal, modifier = Modifier.size(11.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Check Live Feeds", fontSize = 9.sp, color = NeonTeal)
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(CarbonLight.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                .padding(8.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = statusText, color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    Text(text = "${(overallProgress * 100).toInt()}%", color = NeonPink, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = overallProgress,
                    modifier = Modifier.fillMaxWidth().height(3.dp).clip(CircleShape),
                    color = NeonTeal,
                    trackColor = SpaceDark
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        val platforms = listOf(
            PlatformInfo("Spotify Music", "240M+ Active Listeners", Icons.Default.MusicNote),
            PlatformInfo("Apple Music", "Premium HD Audio", Icons.Default.Audiotrack),
            PlatformInfo("JioSaavn", "Largest Indian Audience", Icons.Default.Album),
            PlatformInfo("YouTube Music", "Content ID Protection Included", Icons.Default.PlayArrow),
            PlatformInfo("Wynk Music", "Airtel India Ecosystem", Icons.Default.Radio)
        )

        platforms.forEach { platform ->
            val storeStatus = when (release.status) {
                "Live in Stores" -> StoreStatus("LIVE", SuccessGreen, "Ready to Stream", "100% Royalties Active", true)
                "Delivered to Stores" -> StoreStatus("INDEXING", NeonTeal, "Ingested", "Eta: ~12 hours", false)
                "Approved" -> StoreStatus("QUEUED", NeonViolet, "Metadata QC Approved", "Awaiting ingestion feed", false)
                else -> StoreStatus("PENDING QC", AlertOrange, "Awaiting Staff Review", "Position in queue: #142", false)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CarbonSlate.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .border(1.dp, GlassBorder.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                        .clickable {
                            selectedStoreForLogs = if (selectedStoreForLogs == platform.name) null else platform.name
                        }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(CarbonLight, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = platform.icon,
                            contentDescription = null,
                            tint = if (storeStatus.isLive) SuccessGreen else NeonTeal,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = platform.name,
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = storeStatus.subText,
                            color = TextSecondary,
                            fontSize = 9.sp
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Box(
                            modifier = Modifier
                                .background(storeStatus.color.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = storeStatus.label,
                                color = storeStatus.color,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = storeStatus.extraText,
                            color = TextSecondary,
                            fontSize = 8.sp
                        )
                    }

                    if (storeStatus.isLive) {
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = {
                                Toast.makeText(context, "Streaming link: Live feed verified for ${release.title} on ${platform.name}!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Launch,
                                contentDescription = "Listen",
                                tint = SuccessGreen,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }

                AnimatedVisibility(
                    visible = selectedStoreForLogs == platform.name,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .background(SpaceDark, RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                            .border(1.dp, GlassBorder.copy(alpha = 0.15f), RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            "Live Feed Delivery Logs — ${platform.name.uppercase()}",
                            color = NeonTeal,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        LogLine(time = "[17:39:01]", msg = "Validating metadata & lossless audio hashes...")
                        LogLine(time = "[17:39:03]", msg = "Assigned UPC ${release.upcCode.ifEmpty { "UPC908127391" }} & ISRC ${release.isrcCode.ifEmpty { "IN-ST1-26-00041" }} successfully.")
                        
                        if (release.status == "Pending Review") {
                            LogLine(time = "[17:39:05]", msg = "QC Queue: Position #142 in queue. ST Free review will verify format standards.")
                        } else {
                            LogLine(time = "[QC Approved]", msg = "Audio quality verify passed. Lossless FLAC/WAV check completed.")
                        }

                        if (release.status == "Approved" || release.status == "Delivered to Stores" || release.status == "Live in Stores") {
                            LogLine(time = "[Outbound Feed]", msg = "XML feed package built & pushed to ${platform.name} secure SFTP directory.")
                        }

                        if (release.status == "Delivered to Stores" || release.status == "Live in Stores") {
                            LogLine(time = "[Store Ingest]", msg = "DDEX standard ingest feedback received: CODE 200 SUCCESS.")
                            LogLine(time = "[Store Indexing]", msg = "Indexing metadata & generating search tokens.")
                        }

                        if (release.status == "Live in Stores") {
                            LogLine(time = "[LIVE CONFIRMED]", msg = "Release is officially live. Index URL matching successful. Royalty feed activated!")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LogLine(time: String, msg: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = time, color = NeonPink, fontSize = 7.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        Text(text = msg, color = Color.White.copy(alpha = 0.8f), fontSize = 7.sp, fontFamily = FontFamily.Monospace)
    }
}

data class PlatformInfo(val name: String, val desc: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)
data class StoreStatus(val label: String, val color: Color, val subText: String, val extraText: String, val isLive: Boolean)

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TextSecondary, fontSize = 11.sp)
        Text(text = value, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}


// ==========================================
// TAB 2: DISTRIBUTE WIZARD
// ==========================================
@Composable
fun DistributeTab(
    artistName: String,
    recordLabel: String,
    onDistribute: (String, String, String, String, String, String, String, String, String) -> Unit
) {
    val context = LocalContext.current

    var currentStep by remember { mutableStateOf(1) } // Step 1: Metadata, Step 2: Media, Step 3: Distribution

    // Form states
    var songTitle by remember { mutableStateOf("") }
    var releaseArtist by remember { mutableStateOf(artistName) }
    var featuringArtist by remember { mutableStateOf("") }
    var lyricist by remember { mutableStateOf("") }
    var composer by remember { mutableStateOf("") }
    var genre by remember { mutableStateOf("Pop") }
    var language by remember { mutableStateOf("Hindi") }
    var releaseDate by remember { mutableStateOf("") }
    var labelName by remember { mutableStateOf(recordLabel) }

    // Upload simulation states
    var audioFileUploaded by remember { mutableStateOf(false) }
    var audioUploading by remember { mutableStateOf(false) }
    var audioUploadProgress by remember { mutableStateOf(0.0f) }
    var audioFileName by remember { mutableStateOf("") }
    var audioValidationError by remember { mutableStateOf("") }

    var artFileUploaded by remember { mutableStateOf(false) }
    var artUploading by remember { mutableStateOf(false) }
    var artUploadProgress by remember { mutableStateOf(0.0f) }
    var artFileName by remember { mutableStateOf("") }
    var isArtworkGenerated by remember { mutableStateOf(false) }

    // Audio Playback simulation inside the player
    var isPlayingPreview by remember { mutableStateOf(false) }

    // Dropdowns options
    val genresList = listOf("Pop", "Folk", "Romantic", "Bhojpuri", "Hip Hop", "Classical", "Devotional")
    val languagesList = listOf("Hindi", "Bhojpuri", "Bhojpuri Remix", "Punjabi", "Haryanvi", "English", "Sanskrit")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        // Wizard Progress Steps Indicator
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CarbonSlate, RoundedCornerShape(20.dp))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StepIndicator(step = 1, label = "Metadata", active = currentStep >= 1)
                Spacer(modifier = Modifier.weight(1f).height(2.dp).background(if (currentStep >= 2) NeonTeal else CarbonLight))
                StepIndicator(step = 2, label = "Audio/Art", active = currentStep >= 2)
                Spacer(modifier = Modifier.weight(1f).height(2.dp).background(if (currentStep >= 3) NeonTeal else CarbonLight))
                StepIndicator(step = 3, label = "Launch", active = currentStep >= 3)
            }
        }

        // STEP 1: METADATA FORM
        if (currentStep == 1) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CarbonSlate, RoundedCornerShape(20.dp))
                        .border(1.dp, GlassBorder.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Release Information", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NeonTeal)

                    // Song Title
                    OutlinedTextField(
                        value = songTitle,
                        onValueChange = { songTitle = it },
                        label = { Text("Song / Album Title *") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonTeal,
                            unfocusedBorderColor = TextSecondary,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("input_title"),
                        singleLine = true
                    )

                    // Primary Artist
                    OutlinedTextField(
                        value = releaseArtist,
                        onValueChange = { releaseArtist = it },
                        label = { Text("Primary Artist Name *") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonTeal,
                            unfocusedBorderColor = TextSecondary,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Feature / Lyricist Row
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = featuringArtist,
                            onValueChange = { featuringArtist = it },
                            label = { Text("Featuring") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonTeal,
                                unfocusedBorderColor = TextSecondary,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = lyricist,
                            onValueChange = { lyricist = it },
                            label = { Text("Lyricist") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonTeal,
                                unfocusedBorderColor = TextSecondary,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    // Composer & Label Name
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = composer,
                            onValueChange = { composer = it },
                            label = { Text("Composer") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonTeal,
                                unfocusedBorderColor = TextSecondary,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = labelName,
                            onValueChange = { labelName = it },
                            label = { Text("Record Label") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonTeal,
                                unfocusedBorderColor = TextSecondary,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    // Genre & Language Selectors
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DropdownSelector(
                            label = "Genre",
                            selectedValue = genre,
                            options = genresList,
                            onSelected = { genre = it },
                            modifier = Modifier.weight(1f)
                        )
                        DropdownSelector(
                            label = "Language",
                            selectedValue = language,
                            options = languagesList,
                            onSelected = { language = it },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Date Picker Trigger
                    val calendar = Calendar.getInstance()
                    val datePickerDialog = DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            releaseDate = String.format("%d-%02d-%02d", year, month + 1, dayOfMonth)
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    )

                    OutlinedTextField(
                        value = releaseDate,
                        onValueChange = {},
                        label = { Text("Release Date *") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonTeal,
                            unfocusedBorderColor = TextSecondary,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { datePickerDialog.show() },
                        enabled = false,
                        trailingIcon = {
                            Icon(
                                Icons.Default.DateRange,
                                contentDescription = "Pick Date",
                                tint = NeonTeal,
                                modifier = Modifier.clickable { datePickerDialog.show() }
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            if (songTitle.isBlank() || releaseArtist.isBlank() || releaseDate.isBlank()) {
                                Toast.makeText(context, "Please fill in all starred (*) fields", Toast.LENGTH_SHORT).show()
                            } else {
                                currentStep = 2
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonTeal, contentColor = SpaceDark),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("step1_next")
                    ) {
                        Text("Next: Upload Files", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }

        // STEP 2: AUDIO & ARTWORK UPLOADER (WITH SPINNING 3D VINYL PLAYER)
        if (currentStep == 2) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CarbonSlate, RoundedCornerShape(20.dp))
                        .border(1.dp, GlassBorder.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Upload Media Content",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonTeal,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    // --- DRAG AND DROP MUSIC UPLOADER ZONE ---
                    Text(
                        "Lossless Audio Uploader",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(SpaceDark)
                            .border(1.dp, if (audioFileUploaded) SuccessGreen.copy(alpha = 0.8f) else GlassBorder, RoundedCornerShape(16.dp))
                            .clickable {
                                // Simulate click-to-browse -> load WAV
                                audioUploading = true
                                audioValidationError = ""
                                audioUploadProgress = 0f
                                val timer = Timer()
                                timer.scheduleAtFixedRate(object : TimerTask() {
                                    override fun run() {
                                        audioUploadProgress += 0.2f
                                        if (audioUploadProgress >= 1f) {
                                            audioFileUploaded = true
                                            audioUploading = false
                                            audioFileName = "studio_mix_master.wav"
                                            timer.cancel()
                                        }
                                    }
                                }, 80, 80)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                imageVector = if (audioFileUploaded) Icons.Default.CloudDone else Icons.Default.CloudUpload,
                                contentDescription = null,
                                tint = if (audioFileUploaded) SuccessGreen else NeonTeal,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (audioFileUploaded) "Loaded: $audioFileName" else if (audioUploading) "Uploading... ${ (audioUploadProgress * 100).toInt() }%" else "Drag & Drop WAV/FLAC audio files here",
                                color = if (audioFileUploaded) SuccessGreen else TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = if (audioFileUploaded) "CD-Quality (16-bit / 44.1kHz WAV • Verified)" else "or Click to Browse your folders",
                                color = TextSecondary,
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center
                            )

                            if (audioUploading) {
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = audioUploadProgress,
                                    modifier = Modifier.fillMaxWidth(0.8f).height(4.dp),
                                    color = NeonTeal,
                                    trackColor = CarbonLight
                                )
                            }
                        }
                    }

                    // Simulated drop chips
                    if (!audioUploading) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                "Simulate Audio Drag & Drop (Tap to drop):",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Valid WAV
                                DropChip(
                                    label = "master_24bit.wav",
                                    isLossless = true,
                                    onClick = {
                                        audioUploading = true
                                        audioValidationError = ""
                                        audioFileUploaded = false
                                        audioUploadProgress = 0f
                                        val timer = Timer()
                                        timer.scheduleAtFixedRate(object : TimerTask() {
                                            override fun run() {
                                                audioUploadProgress += 0.25f
                                                if (audioUploadProgress >= 1f) {
                                                    audioFileUploaded = true
                                                    audioUploading = false
                                                    audioFileName = "master_24bit.wav"
                                                    timer.cancel()
                                                }
                                            }
                                        }, 60, 60)
                                    }
                                )
                                // Valid FLAC
                                DropChip(
                                    label = "album_version.flac",
                                    isLossless = true,
                                    onClick = {
                                        audioUploading = true
                                        audioValidationError = ""
                                        audioFileUploaded = false
                                        audioUploadProgress = 0f
                                        val timer = Timer()
                                        timer.scheduleAtFixedRate(object : TimerTask() {
                                            override fun run() {
                                                audioUploadProgress += 0.25f
                                                if (audioUploadProgress >= 1f) {
                                                    audioFileUploaded = true
                                                    audioUploading = false
                                                    audioFileName = "album_version.flac"
                                                    timer.cancel()
                                                }
                                            }
                                        }, 60, 60)
                                    }
                                )
                                // Invalid MP3
                                DropChip(
                                    label = "draft_preview.mp3",
                                    isLossless = false,
                                    onClick = {
                                        audioFileUploaded = false
                                        audioFileName = ""
                                        audioValidationError = "Format Error: draft_preview.mp3 is a compressed lossy format (MP3). To maintain high dynamic range and prevent acoustic compression artifacts, ST Free Panel requires high-quality lossless formats like WAV (16-bit/24-bit PCM) or FLAC."
                                        Toast.makeText(context, "Validation Failed: WAV or FLAC required!", Toast.LENGTH_SHORT).show()
                                    }
                                )
                                // Invalid M4A
                                DropChip(
                                    label = "vocal_rough.m4a",
                                    isLossless = false,
                                    onClick = {
                                        audioFileUploaded = false
                                        audioFileName = ""
                                        audioValidationError = "Format Error: vocal_rough.m4a is a compressed AAC/M4A format. Professional distribution strictly requires uncompressed master audio files (.wav or .flac) to pass store requirements."
                                        Toast.makeText(context, "Validation Failed: WAV or FLAC required!", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }

                    // Validation Error Card
                    if (audioValidationError.isNotEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0x13FF1744)),
                            border = BorderStroke(1.dp, Color(0x55FF1744)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Warning",
                                        tint = Color(0xFFFF1744),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Validation Check Failed",
                                        color = Color(0xFFFF1744),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = audioValidationError,
                                    color = TextPrimary,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "💡 Tip: You can tap on 'master_24bit.wav' or 'album_version.flac' chips above to simulate a successful high-quality file upload.",
                                    color = TextSecondary,
                                    fontSize = 9.sp,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    HorizontalDivider(color = CarbonLight, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(4.dp))

                    // --- COVER ART & PLACEHOLDER GENERATOR SECTION ---
                    Text(
                        "Artwork / Cover Image",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    if (artFileUploaded) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (isArtworkGenerated) {
                                // Dynamic Live Preview of Placeholder Art
                                ArtworkPlaceholderCard(
                                    title = songTitle,
                                    artist = releaseArtist,
                                    modifier = Modifier.size(160.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "✨ Dynamic Art Placeholder Active",
                                    color = SuccessGreen,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                // Normal simulated image card
                                Box(
                                    modifier = Modifier
                                        .size(160.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(CarbonLight)
                                        .border(1.dp, GlassBorder, RoundedCornerShape(16.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.Photo,
                                            contentDescription = null,
                                            tint = NeonTeal,
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("custom_artwork.jpg", color = TextPrimary, fontSize = 11.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedButton(
                                onClick = {
                                    artFileUploaded = false
                                    isArtworkGenerated = false
                                    artFileName = ""
                                },
                                border = BorderStroke(1.dp, Color(0xFFFF1744).copy(alpha = 0.5f)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("Remove Artwork", color = Color(0xFFFF1744), fontSize = 10.sp)
                            }
                        }
                    } else {
                        // Artwork Actions
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Generator Button
                            Button(
                                onClick = {
                                    isArtworkGenerated = true
                                    artFileUploaded = true
                                    artFileName = "ST_Generated_Artwork.png"
                                    Toast.makeText(context, "✨ Album Art Placeholder generated based on metadata!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonTeal, contentColor = Color.White),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(42.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("✨ Generate Art Placeholder", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            // Manual select
                            OutlinedButton(
                                onClick = {
                                    artUploading = true
                                    artUploadProgress = 0f
                                    isArtworkGenerated = false
                                    val timer = Timer()
                                    timer.scheduleAtFixedRate(object : TimerTask() {
                                        override fun run() {
                                            artUploadProgress += 0.2f
                                            if (artUploadProgress >= 1f) {
                                                artFileUploaded = true
                                                artUploading = false
                                                artFileName = "custom_cover.png"
                                                timer.cancel()
                                            }
                                        }
                                    }, 80, 80)
                                },
                                border = BorderStroke(1.dp, NeonTeal),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(42.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Photo,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = NeonTeal
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Choose Custom Image (Min 1400x1400)", color = NeonTeal, fontSize = 12.sp)
                            }

                            if (artUploading) {
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = artUploadProgress,
                                    modifier = Modifier.fillMaxWidth().height(4.dp),
                                    color = NeonTeal,
                                    trackColor = CarbonLight
                                )
                            }
                        }
                    }

                    // Interactive Custom 3D Spinning Vinyl Record Player Preview!
                    if (audioFileUploaded && artFileUploaded) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Preview Live Pre-Distribution Art & Sound",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonTeal,
                            modifier = Modifier.align(Alignment.Start)
                        )

                        // 3D Player box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SpaceDark, RoundedCornerShape(16.dp))
                                .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // 3D Vinyl draw
                                SpinningVinylPlayer(isPlaying = isPlayingPreview, songTitle = songTitle, releaseArtist = releaseArtist)

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = songTitle,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = releaseArtist,
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Controls
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    IconButton(
                                        onClick = { isPlayingPreview = !isPlayingPreview },
                                        modifier = Modifier
                                            .background(NeonTeal, CircleShape)
                                            .size(48.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isPlayingPreview) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = "Play Preview",
                                            tint = SpaceDark
                                        )
                                    }
                                }

                                // Interactive Glowing Frequency wave animation
                                if (isPlayingPreview) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    WaveformAnimation()
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { currentStep = 1 },
                            border = BorderStroke(1.dp, NeonTeal),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Text("Back", color = NeonTeal)
                        }

                        Button(
                            onClick = {
                                if (!audioFileUploaded || !artFileUploaded) {
                                    Toast.makeText(context, "Please upload both Audio & Cover Art files to proceed.", Toast.LENGTH_SHORT).show()
                                } else {
                                    currentStep = 3
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonTeal, contentColor = SpaceDark),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("step2_next")
                        ) {
                            Text("Next: Distribute", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // STEP 3: PLATFORMS CHECKLIST & DISTRIBUTE
        if (currentStep == 3) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CarbonSlate, RoundedCornerShape(20.dp))
                        .border(1.dp, GlassBorder.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Select Distribution Networks", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NeonTeal)
                    Text(
                        "Your music will be delivered absolutely free with 100% royalty-keep to all selected platforms globally:",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    val platformsList = listOf(
                        "Spotify (Global Digital)",
                        "JioSaavn (India Native)",
                        "Wynk Music (Airtel India)",
                        "YouTube Music & Content ID",
                        "Apple Music (Global HD)",
                        "Amazon Music & Prime Play",
                        "Instagram & Facebook Reels",
                        "TikTok & Resso ByteDance"
                    )

                    platformsList.forEach { platform ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SpaceDark.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CloudQueue, contentDescription = null, tint = NeonTeal, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(platform, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(Icons.Default.CheckCircle, contentDescription = "Active", tint = SuccessGreen, modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Legal checklist
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = NeonPink, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "I confirm that I own 100% copyrights of this musical work.",
                            fontSize = 11.sp,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { currentStep = 2 },
                            border = BorderStroke(1.dp, NeonTeal),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Text("Back", color = NeonTeal)
                        }

                        Button(
                            onClick = {
                                onDistribute(
                                    songTitle,
                                    releaseArtist,
                                    featuringArtist,
                                    lyricist,
                                    composer,
                                    genre,
                                    language,
                                    releaseDate,
                                    labelName
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonPink, contentColor = TextPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("btn_submit_release")
                        ) {
                            Text("Launch Track Free!", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
fun StepIndicator(step: Int, label: String, active: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(if (active) NeonTeal else CarbonLight, CircleShape)
                .border(1.dp, if (active) NeonTeal else TextSecondary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                step.toString(),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = if (active) SpaceDark else TextSecondary
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            label,
            fontSize = 9.sp,
            color = if (active) NeonTeal else TextSecondary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun UploaderRow(
    title: String,
    uploaded: Boolean,
    uploading: Boolean,
    progress: Float,
    onSelectFile: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SpaceDark, RoundedCornerShape(14.dp))
            .border(1.dp, if (uploaded) SuccessGreen else GlassBorder, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (uploaded) Icons.Default.CloudDone else Icons.Default.CloudUpload,
                contentDescription = null,
                tint = if (uploaded) SuccessGreen else NeonTeal
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = if (uploaded) "File uploaded successfully" else if (uploading) "Processing file..." else "No file selected",
                    color = if (uploaded) SuccessGreen else TextSecondary,
                    fontSize = 11.sp
                )
            }
            if (!uploaded && !uploading) {
                Button(
                    onClick = onSelectFile,
                    colors = ButtonDefaults.buttonColors(containerColor = CarbonLight),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Choose", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonTeal)
                }
            }
        }

        if (uploading) {
            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(CircleShape),
                color = NeonTeal,
                trackColor = CarbonLight
            )
        }
    }
}

@Composable
fun DropChip(
    label: String,
    isLossless: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .border(
                width = 1.dp,
                color = if (isLossless) SuccessGreen.copy(alpha = 0.5f) else Color(0xFFFF1744).copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() },
        color = if (isLossless) SuccessGreen.copy(alpha = 0.05f) else Color(0xFFFF1744).copy(alpha = 0.05f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(if (isLossless) SuccessGreen else Color(0xFFFF1744), CircleShape)
            )
            Text(
                text = label,
                color = TextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ArtworkPlaceholderCard(
    title: String,
    artist: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(NeonPink, NeonTeal, NeonViolet)
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        // Decorative concentric rings
        Box(
            modifier = Modifier
                .fillMaxSize(0.85f)
                .border(1.dp, Color.White.copy(alpha = 0.08f), CircleShape)
        )
        Box(
            modifier = Modifier
                .fillMaxSize(0.65f)
                .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
        )
        Box(
            modifier = Modifier
                .fillMaxSize(0.45f)
                .background(Color.White.copy(alpha = 0.08f), CircleShape)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title.ifEmpty { "UNTITLED SINGLE" }.uppercase(),
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                letterSpacing = 1.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = artist.ifEmpty { "Independent Artist" }.uppercase(),
                    color = Color.White,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "100% FREE • ST PANEL",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 6.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun DropdownSelector(
    label: String,
    selectedValue: String,
    options: List<String>,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = selectedValue,
            onValueChange = {},
            label = { Text(label) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonTeal,
                unfocusedBorderColor = TextSecondary,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            readOnly = true,
            trailingIcon = {
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = NeonTeal,
                    modifier = Modifier.clickable { expanded = true }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(CarbonSlate)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, color = TextPrimary) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

// 3D Canvas Spinning Vinyl Disc
@Composable
fun SpinningVinylPlayer(isPlaying: Boolean, songTitle: String = "", releaseArtist: String = "") {
    val infiniteTransition = rememberInfiniteTransition(label = "vinyl_spin")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle_animation"
    )

    val toneArmAngle by animateFloatAsState(
        targetValue = if (isPlaying) 28f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
    )

    Box(
        modifier = Modifier
            .size(200.dp)
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        // Draw physical vinyl components via canvas
        Canvas(modifier = Modifier
            .fillMaxSize()
            .rotate(if (isPlaying) angle else 0f)
        ) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.minDimension / 2

            // 1. Base Record (Black Vinyl disc with realistic subtle depth shading)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF18181A), Color(0xFF030303)),
                    center = center,
                    radius = radius
                ),
                radius = radius
            )

            // 2. Shiny metallic record grooves (concentric circles)
            for (r in (radius * 0.4).toInt()..(radius * 0.95).toInt() step 6) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.08f),
                    radius = r.toFloat(),
                    style = Stroke(width = 0.8f)
                )
            }

            // 3. Center sticker badge
            drawCircle(
                brush = Brush.linearGradient(listOf(NeonTeal, NeonViolet)),
                radius = radius * 0.3f
            )

            // Center sticker text marker
            drawCircle(
                color = Color.Black,
                radius = radius * 0.08f
            )
        }

        // Center sticker dynamic text rotating with record
        if (songTitle.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .size(60.dp)
                    .rotate(if (isPlaying) angle else 0f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = songTitle.take(10).uppercase(),
                    color = Color.White,
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = releaseArtist.take(12),
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Tone-Arm needle (Overlay drawn on top pivot to animate landing on vinyl)
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = 10.dp, y = (-10).dp)
        ) {
            val center = Offset(size.width * 0.75f, size.height * 0.15f)
            val pivotRadius = 14f

            // Tone arm rotation logic
            val rad = Math.toRadians(toneArmAngle.toDouble() - 45.0)
            val armLength = size.width * 0.5f
            val endPoint = Offset(
                (center.x + armLength * cos(rad)).toFloat(),
                (center.y + armLength * sin(rad)).toFloat()
            )

            // Needle metal body
            drawLine(
                color = Color.LightGray,
                start = center,
                end = endPoint,
                strokeWidth = 5f
            )

            // Shiny arm head / shell cartridge
            drawCircle(
                color = NeonPink,
                radius = 8f,
                center = endPoint
            )

            // Base Pivot holder
            drawCircle(
                color = Color.DarkGray,
                radius = pivotRadius,
                center = center
            )
            drawCircle(
                color = Color.LightGray,
                radius = pivotRadius * 0.5f,
                center = center
            )
        }
    }
}

// Interactive Audio waveform bar animation
@Composable
fun WaveformAnimation() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "waveform")
        val itemsCount = 20

        for (i in 0 until itemsCount) {
            val heightScale by infiniteTransition.animateFloat(
                initialValue = 0.1f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 300 + (i * 30),
                        easing = FastOutSlowInEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar_$i"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(heightScale)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Brush.verticalGradient(listOf(NeonTeal, NeonViolet)))
            )
        }
    }
}


data class D3ChartData(
    val platformName: String,
    val sharePercent: Float,
    val streamCount: Long,
    val royaltyValue: Float,
    val color: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun D3InspiredDistributionChart(yearlyReleases: Float) {
    var selectedMetric by remember { mutableStateOf(0) } // 0: Share %, 1: Streams, 2: Royalties
    var selectedPlatform by remember { mutableStateOf("Spotify Music") }

    val totalStreamsBase = (yearlyReleases * 32000L).toLong()

    val platformData = remember(yearlyReleases) {
        listOf(
            D3ChartData(
                platformName = "Spotify Music",
                sharePercent = 0.38f,
                streamCount = (totalStreamsBase * 0.38f).toLong(),
                royaltyValue = (totalStreamsBase * 0.38f * 0.12f),
                color = Color(0xFF1DB954), // Spotify Green
                icon = Icons.Default.MusicNote
            ),
            D3ChartData(
                platformName = "Apple Music",
                sharePercent = 0.22f,
                streamCount = (totalStreamsBase * 0.22f).toLong(),
                royaltyValue = (totalStreamsBase * 0.22f * 0.16f),
                color = Color(0xFFFC3C44), // Apple Red/Pink
                icon = Icons.Default.Audiotrack
            ),
            D3ChartData(
                platformName = "JioSaavn",
                sharePercent = 0.18f,
                streamCount = (totalStreamsBase * 0.18f).toLong(),
                royaltyValue = (totalStreamsBase * 0.18f * 0.08f),
                color = Color(0xFF00B2FF), // JioSaavn Cyan
                icon = Icons.Default.Album
            ),
            D3ChartData(
                platformName = "YouTube Music",
                sharePercent = 0.14f,
                streamCount = (totalStreamsBase * 0.14f).toLong(),
                royaltyValue = (totalStreamsBase * 0.14f * 0.10f),
                color = Color(0xFFFF0000), // YouTube Red
                icon = Icons.Default.PlayArrow
            ),
            D3ChartData(
                platformName = "Wynk Music",
                sharePercent = 0.08f,
                streamCount = (totalStreamsBase * 0.08f).toLong(),
                royaltyValue = (totalStreamsBase * 0.08f * 0.07f),
                color = Color(0xFF9C27B0), // Wynk Purple
                icon = Icons.Default.Radio
            )
        )
    }

    val activeData = platformData.firstOrNull { it.platformName == selectedPlatform } ?: platformData[0]

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("d3_visualization_chart_card"),
        colors = CardDefaults.cardColors(containerColor = CarbonSlate),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, GlassBorder)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Distribution Metrics Chart",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Dynamic Native Visualizer • Powered by ST Free",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
                Box(
                    modifier = Modifier
                        .background(NeonPink.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .border(1.dp, NeonPink.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "LIVE DATA",
                        color = NeonPink,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Metric Focus Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SpaceDark, RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("Audience Share %", "Monthly Streams", "Your Royalties (₹)").forEachIndexed { index, label ->
                    val isSelected = selectedMetric == index
                    val tabBg by animateColorAsState(
                        targetValue = if (isSelected) CarbonLight else Color.Transparent,
                        label = "tabBg"
                    )
                    val tabTextAndIconColor by animateColorAsState(
                        targetValue = if (isSelected) NeonTeal else TextSecondary,
                        label = "tabText"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(tabBg)
                            .clickable { selectedMetric = index }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = tabTextAndIconColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Adaptive row/column layout
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val isWide = maxWidth > 360.dp

                if (isWide) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Custom Animated Donut
                        val platformNames = remember { platformData.map { it.platformName } }
                        Box(
                            modifier = Modifier
                                .weight(1.1f)
                                .aspectRatio(1f)
                                .clickable {
                                    val currentIndex = platformNames.indexOf(selectedPlatform)
                                    val nextIndex = (currentIndex + 1) % platformNames.size
                                    selectedPlatform = platformNames[nextIndex]
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            DonutChartCanvas(
                                platformData = platformData,
                                selectedPlatform = selectedPlatform,
                                onPlatformSelected = { selectedPlatform = it }
                            )

                            // Inner central display
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(activeData.color.copy(alpha = 0.1f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = activeData.icon,
                                        contentDescription = null,
                                        tint = activeData.color,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = activeData.platformName.replace(" Music", ""),
                                    color = TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                val displayValue = when (selectedMetric) {
                                    0 -> String.format("%.0f%% Share", activeData.sharePercent * 100)
                                    1 -> String.format("%,d streams", activeData.streamCount)
                                    else -> String.format("₹%,d", activeData.royaltyValue.toInt())
                                }
                                Text(
                                    text = displayValue,
                                    color = activeData.color,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Right: Interactive legend list
                        Column(
                            modifier = Modifier.weight(1.3f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            platformData.forEach { data ->
                                LegendItemRow(
                                    data = data,
                                    selectedMetric = selectedMetric,
                                    isSelected = selectedPlatform == data.platformName,
                                    onClick = { selectedPlatform = data.platformName }
                                )
                            }
                        }
                    }
                } else {
                    // Portrait Layout: Stack vertically
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val platformNames = remember { platformData.map { it.platformName } }
                        Box(
                            modifier = Modifier
                                .size(150.dp)
                                .aspectRatio(1f)
                                .clickable {
                                    val currentIndex = platformNames.indexOf(selectedPlatform)
                                    val nextIndex = (currentIndex + 1) % platformNames.size
                                    selectedPlatform = platformNames[nextIndex]
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            DonutChartCanvas(
                                platformData = platformData,
                                selectedPlatform = selectedPlatform,
                                onPlatformSelected = { selectedPlatform = it }
                            )

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = activeData.platformName.replace(" Music", ""),
                                    color = TextPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                val displayValue = when (selectedMetric) {
                                    0 -> String.format("%.0f%% Share", activeData.sharePercent * 100)
                                    1 -> String.format("%,d", activeData.streamCount)
                                    else -> String.format("₹%,d", activeData.royaltyValue.toInt())
                                }
                                Text(
                                    text = displayValue,
                                    color = activeData.color,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            platformData.forEach { data ->
                                LegendItemRow(
                                    data = data,
                                    selectedMetric = selectedMetric,
                                    isSelected = selectedPlatform == data.platformName,
                                    onClick = { selectedPlatform = data.platformName }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Highlights of Royalty Saved Comparison based on 100% payout!
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SpaceDark, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ST 100% Royalty Protection Value",
                            color = NeonTeal,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Verified",
                            tint = SuccessGreen,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    val activePlatformCommissionSaved = activeData.royaltyValue * 0.20f
                    
                    Text(
                        text = "Because ST Digital does not take a 20% royalty share, you kept an extra ₹${String.format("%,d", activePlatformCommissionSaved.toInt())} from ${activeData.platformName} alone! Paid distributors would have pocketed this as an annual recurring commission.",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun DonutChartCanvas(
    platformData: List<D3ChartData>,
    selectedPlatform: String,
    onPlatformSelected: (String) -> Unit
) {
    val animationProgress = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(1200, easing = FastOutSlowInEasing)
        )
    }

    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2.3f
        var startAngle = -90f // Start drawing from 12 o'clock

        platformData.forEach { data ->
            val sweepAngle = data.sharePercent * 360f * animationProgress.value
            val isSelected = selectedPlatform == data.platformName
            val strokeWidth = if (isSelected) 18.dp.toPx() else 11.dp.toPx()

            // Outer glow if selected
            if (isSelected) {
                drawArc(
                    color = data.color.copy(alpha = 0.2f),
                    startAngle = startAngle - 1f,
                    sweepAngle = sweepAngle + 2f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth + 8.dp.toPx())
                )
            }

            // Normal arc
            drawArc(
                color = data.color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth)
            )

            startAngle += sweepAngle
        }
    }
}

@Composable
fun LegendItemRow(
    data: D3ChartData,
    selectedMetric: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundBg by animateColorAsState(
        targetValue = if (isSelected) CarbonLight.copy(alpha = 0.6f) else Color.Transparent,
        label = "legendBg"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundBg)
            .clickable { onClick() }
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(data.color, CircleShape)
            )
            Text(
                text = data.platformName,
                color = if (isSelected) TextPrimary else TextSecondary,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        val displayVal = when (selectedMetric) {
            0 -> String.format("%.0f%%", data.sharePercent * 100)
            1 -> String.format("%,d", data.streamCount)
            else -> String.format("₹%,d", data.royaltyValue.toInt())
        }

        Text(
            text = displayVal,
            color = if (isSelected) data.color else TextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}


// ==========================================
// TAB 3: STORES & SAVINGS CALCULATOR
// ==========================================
fun generateAgreementPdf(context: android.content.Context, artistName: String, recordLabel: String): Boolean {
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas
    val paint = AndroidPaint().apply {
        isAntiAlias = true
    }

    var y = 60f

    // Helper to draw centered bold title
    fun drawTitle(text: String, size: Float, isBold: Boolean = true) {
        paint.textSize = size
        paint.typeface = if (isBold) Typeface.create(Typeface.DEFAULT, Typeface.BOLD) else Typeface.DEFAULT
        paint.color = android.graphics.Color.BLACK
        val width = paint.measureText(text)
        canvas.drawText(text, (595f - width) / 2f, y, paint)
        y += size + 10f
    }

    // Helper to draw horizontal line
    fun drawLine(thickness: Float, spacingAfter: Float) {
        paint.color = android.graphics.Color.LTGRAY
        paint.strokeWidth = thickness
        canvas.drawLine(40f, y, 555f, y, paint)
        y += spacingAfter
    }

    // Helper to draw body text with auto word-wrapping
    fun drawBodyText(text: String, size: Float = 10f, isBold: Boolean = false, isItalic: Boolean = false) {
        paint.textSize = size
        val tf = if (isBold && isItalic) Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
                 else if (isBold) Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                 else if (isItalic) Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                 else Typeface.DEFAULT
        paint.typeface = tf
        paint.color = android.graphics.Color.DKGRAY

        val words = text.split(" ")
        var line = ""
        for (word in words) {
            val testLine = if (line.isEmpty()) word else "$line $word"
            if (paint.measureText(testLine) > 515f) {
                canvas.drawText(line, 40f, y, paint)
                y += size + 4f
                line = word
            } else {
                line = testLine
            }
        }
        if (line.isNotEmpty()) {
            canvas.drawText(line, 40f, y, paint)
            y += size + 6f
        }
    }

    try {
        // Document content drawing
        drawTitle("ST DIGITAL", 14f)
        drawTitle("FREE MUSIC DISTRIBUTION AGREEMENT", 18f)
        drawLine(1.5f, 20f)

        drawBodyText("This legally binding Music Distribution Agreement (the \"Agreement\") is entered into by and between:", 10f, isItalic = true)
        y += 10f

        drawBodyText("DISTRIBUTOR:", 11f, isBold = true)
        drawBodyText("ST Digital Distribution Platform (including its global licensing networks).", 10f)
        y += 6f

        drawBodyText("ARTIST / LICENSOR:", 11f, isBold = true)
        drawBodyText(if (artistName.isBlank()) "Independent Musician / Creator" else artistName, 10f)
        y += 6f

        drawBodyText("RECORD LABEL:", 11f, isBold = true)
        drawBodyText(if (recordLabel.isBlank()) "Self-Published Independent Label" else recordLabel, 10f)
        y += 10f

        drawBodyText("EFFECTIVE DATE: June 29, 2026", 11f, isBold = true)
        drawLine(1f, 15f)

        drawBodyText("TERMS AND CONDITIONS:", 12f, isBold = true)
        y += 5f

        drawBodyText("1. GRANT OF DISTRIBUTION RIGHTS", 11f, isBold = true)
        drawBodyText("The Licensor hereby grants to ST Digital the non-exclusive, worldwide right to distribute, encode, transmit, promote, and sell the musical works, sound recordings, and cover artwork uploaded by the Licensor to digital streaming platforms (DSPs) including Spotify, JioSaavn, Wynk Music, YouTube Music, Apple Music, and others.", 10f)
        y += 8f

        drawBodyText("2. 100% ROYALTY POLICY GUARANTEE (COMMISSION-FREE)", 11f, isBold = true)
        drawBodyText("ST Digital guarantees that the Licensor shall retain exactly one hundred percent (100%) of all royalties, license fees, mechanical shares, and subscription revenues collected directly from DSPs. ST Digital shall charge ZERO (0%) commission on standard digital distribution, ZERO hidden fees, and ZERO recurring yearly registration charges. Every single rupee earned goes directly to the Licensor.", 10f, isBold = true)
        y += 8f

        drawBodyText("3. METADATA OWNERSHIP & MASTER INTELLECTUAL PROPERTY", 11f, isBold = true)
        drawBodyText("The Licensor retains absolute ownership of all sound recordings, master rights, composition lyrics, and underlying publishing copyrights. ST Digital acts strictly as a distribution partner, metadata pipeline, and royalty aggregator, holding no claim to the Artist's intellectual property.", 10f)
        y += 8f

        drawBodyText("4. COMPREHENSIVE CORRECTION & TAKEDOWN RIGHTS", 11f, isBold = true)
        drawBodyText("The Licensor has the absolute right to request updates, spelling corrections, metadata revisions, or a complete take-down of distributed works at any time. ST Digital agrees to process and dispatch take-down instructions to major platforms within 7 business days, without charging any exit or penalty fees.", 10f)
        y += 8f

        drawBodyText("5. AUTOMATED INGESTION & DATA REPORTING", 11f, isBold = true)
        drawBodyText("ST Digital agrees to utilize advanced DDEX ingestion protocols to process all submissions, prioritizing store ingestion with industry-leading speeds (24 to 48 hours for Spotify/YouTube). Ingest statistics and stream reports shall be delivered monthly to the Licensor's hub.", 10f)
        y += 12f

        drawLine(1f, 15f)
        drawBodyText("IN WITNESS WHEREOF, the parties hereto have executed and approved this Free Distribution Agreement electronically as of the Effective Date.", 9f, isItalic = true)
        y += 30f

        // Draw signatures columns
        paint.textSize = 10f
        paint.color = android.graphics.Color.DKGRAY
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

        // Column 1: Distributor
        canvas.drawText("ST Digital Distribution Board", 40f, y, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 9f
        canvas.drawText("Digitally Signed & Validated", 40f, y + 16f, paint)
        canvas.drawText("ST Licensing Authority", 40f, y + 28f, paint)

        // Column 2: Artist
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 10f
        canvas.drawText(if (artistName.isBlank()) "Independent Artist" else artistName, 320f, y, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 9f
        canvas.drawText("Electronically Authenticated", 320f, y + 16f, paint)
        canvas.drawText("Licensor Signature", 320f, y + 28f, paint)

        pdfDocument.finishPage(page)

        // Save file to Public Downloads using MediaStore on API 29+
        val filename = "ST_Digital_Free_Distribution_Agreement.pdf"
        var savedSuccessfully = false

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                resolver.openOutputStream(uri).use { outputStream ->
                    if (outputStream != null) {
                        pdfDocument.writeTo(outputStream)
                        savedSuccessfully = true
                    }
                }
            }
        }

        // Fallback for older versions or if MediaStore failed
        if (!savedSuccessfully) {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }
            val file = File(downloadsDir, filename)
            FileOutputStream(file).use { outputStream ->
                pdfDocument.writeTo(outputStream)
                savedSuccessfully = true
            }
        }

        // Double fallback: save to App's public-facing sandbox files downloads dir so they can access it
        val sandboxFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), filename)
        FileOutputStream(sandboxFile).use { outputStream ->
            pdfDocument.writeTo(outputStream)
        }

        pdfDocument.close()
        return true
    } catch (e: Exception) {
        pdfDocument.close()
        e.printStackTrace()
        return false
    }
}

@Composable
fun StoresTab(artistName: String, recordLabel: String) {
    var yearlyReleases by remember { mutableFloatStateOf(5.0f) }
    var isGeneratingPdf by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Live savings computations
    // Typical paid distributor costs: ₹999 registration fee per year + approx ₹500 store commissions + 20% royalty share
    val paidCost = (yearlyReleases * 999)
    val commissionSavedEstimate = (yearlyReleases * 450)
    val totalCashSaved = paidCost + commissionSavedEstimate

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        // Dashboard Info Card
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CarbonSlate, RoundedCornerShape(24.dp))
                    .border(1.dp, GlassBorder, RoundedCornerShape(24.dp))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "ST Digital Muft Comparison",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonTeal
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Koi Chhupa Charge Nahi • 100% Bilkul Free!",
                    fontSize = 11.sp,
                    color = SuccessGreen,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Interactive Slider
                Text(
                    text = "Songs Released Per Year: ${yearlyReleases.toInt()}",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Slider(
                    value = yearlyReleases,
                    onValueChange = { yearlyReleases = it },
                    valueRange = 1f..50f,
                    colors = SliderDefaults.colors(
                        thumbColor = NeonPink,
                        activeTrackColor = NeonTeal,
                        inactiveTrackColor = CarbonLight
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Output metrics (Saved amount 3D look)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SpaceDark, RoundedCornerShape(16.dp))
                        .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "YOU SAVE ANNUALLY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                        Text(
                            text = String.format("₹%,d", totalCashSaved.toInt()),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = SuccessGreen
                        )
                        Text(
                            text = "+ Keep 100% of your royalty income!",
                            fontSize = 11.sp,
                            color = NeonTeal,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Interactive Live Release Distribution Metrics Chart
        item {
            D3InspiredDistributionChart(yearlyReleases = yearlyReleases)
        }

        // Pricing Matrix Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Paid card
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(CarbonSlate, RoundedCornerShape(18.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(18.dp))
                        .padding(14.dp)
                ) {
                    Text("Paid Distributors", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("₹999/Yr Fees", color = Color.Red, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Commission taken", color = TextSecondary, fontSize = 10.sp)
                }

                // Free card
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(CarbonSlate, RoundedCornerShape(18.dp))
                        .border(1.dp, NeonTeal, RoundedCornerShape(18.dp))
                        .padding(14.dp)
                ) {
                    Text("ST Free Panel", color = NeonTeal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("₹0 / Bilkul Free", color = SuccessGreen, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Keep 100% Royalties", color = NeonTeal, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Royalty & Distribution Agreement PDF Widget
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("royalty_agreement_card"),
                colors = CardDefaults.cardColors(containerColor = CarbonSlate),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, GlassBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(NeonTeal.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified Icon",
                                tint = NeonTeal,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = "100% Royalty Protection Agreement",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Secure your legal commission-free distribution certificate",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Every artist on ST Digital is legally protected under our commission-free guarantee. You own 100% of your copyright masters, publishing rights, and receive 100% payout with absolutely zero annual subscription costs or hidden deductions.",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SpaceDark.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "CONTRACTING PARTIES",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonPink
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Distributor: ST Digital Licensing",
                                fontSize = 10.sp,
                                color = TextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Licensor: ${if (artistName.isBlank()) "Independent Artist" else artistName}",
                                fontSize = 10.sp,
                                color = TextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Box(
                            modifier = Modifier
                                .background(SuccessGreen.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "COMMISSION-FREE",
                                fontSize = 8.sp,
                                color = SuccessGreen,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            isGeneratingPdf = true
                            scope.launch {
                                delay(1200) // Realistic compilation lag
                                val success = generateAgreementPdf(context, artistName, recordLabel)
                                isGeneratingPdf = false
                                if (success) {
                                    showSuccessDialog = true
                                } else {
                                    Toast.makeText(context, "Error saving PDF. Please check storage.", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonTeal, contentColor = SpaceDark),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("download_agreement_button"),
                        enabled = !isGeneratingPdf
                    ) {
                        if (isGeneratingPdf) {
                            CircularProgressIndicator(
                                color = SpaceDark,
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Signing Document...", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(
                                imageVector = Icons.Default.GetApp,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generate & Download Agreement (PDF)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Stores Showcase Grid Headers
        item {
            Text(
                "Connected Global Streaming Networks",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        // Stores List
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StoreDetailCard(name = "Spotify Music", details = "Global Reach • Delivered in 24 Hours", royalties = "100% Kept")
                StoreDetailCard(name = "JioSaavn Music", details = "Largest Indian Audio Library", royalties = "100% Kept")
                StoreDetailCard(name = "Wynk Music", details = "Delivered directly to Airtel Music App", royalties = "100% Kept")
                StoreDetailCard(name = "YouTube Music & Content ID", details = "Protects & Monetizes User Uploads", royalties = "100% Kept")
                StoreDetailCard(name = "Apple Music", details = "HD Audio Global Premium Library", royalties = "100% Kept")
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Agreement Signed! 📜",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "ST_Digital_Free_Distribution_Agreement.pdf has been generated and saved successfully to your Downloads directory.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "ROYALTY COMPLIANCE RECEIPT:",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonPink
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SpaceDark, RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("• 100% Royalty Protection: Active", fontSize = 10.sp, color = TextPrimary)
                            Text("• Platform Commission Rate: 0.00%", fontSize = 10.sp, color = TextPrimary)
                            Text("• Master Intellectual Ownership: 100% Retained", fontSize = 10.sp, color = TextPrimary)
                            Text("• Exit & Takedown Penalties: ₹0 / None", fontSize = 10.sp, color = TextPrimary)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonTeal, contentColor = SpaceDark)
                ) {
                    Text("Awesome", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(android.content.Intent.EXTRA_SUBJECT, "ST Digital Free Distribution Agreement")
                            putExtra(
                                android.content.Intent.EXTRA_TEXT,
                                "I officially registered my artist profile under ST Digital's 100% commission-free guarantee. Kept all my royalties and ownership! Get your free agreement copy inside the app."
                            )
                        }
                        context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Agreement Confirmation"))
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(12.dp))
                        Text("Share Confirmation", color = NeonTeal)
                    }
                }
            },
            containerColor = CarbonSlate,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
        )
    }
}

@Composable
fun StoreDetailCard(name: String, details: String, royalties: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CarbonSlate, RoundedCornerShape(14.dp))
            .border(1.dp, GlassBorder.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.CloudCircle, contentDescription = null, tint = NeonTeal, modifier = Modifier.size(32.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(name, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(details, color = TextSecondary, fontSize = 10.sp)
        }
        Text(
            text = royalties,
            color = SuccessGreen,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .background(SuccessGreen.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}


// ==========================================
// NEWSLETTER SUBSCRIPTION FOOTER COMPONENT
// ==========================================
@Composable
fun NewsletterSubscriptionFooter(
    isSubscribed: Boolean,
    subscribedEmail: String,
    onSubscribe: (String) -> Unit,
    onUnsubscribe: () -> Unit
) {
    var emailInput by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("newsletter_footer_card"),
        colors = CardDefaults.cardColors(containerColor = CarbonSlate),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, GlassBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!isSubscribed) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(NeonPink.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Newsletter Icon",
                        tint = NeonPink,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Streaming Integration Updates",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Be the first to know when we integrate new streaming stores, optimize DDEX ingestion speeds, or update royalty algorithms.",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = emailInput,
                    onValueChange = {
                        emailInput = it
                        if (emailError != null) emailError = null
                    },
                    label = { Text("Enter your artist email") },
                    placeholder = { Text("artist@yourdomain.com") },
                    isError = emailError != null,
                    supportingText = if (emailError != null) {
                        { Text(emailError!!, color = Color.Red, fontSize = 10.sp) }
                    } else null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonTeal,
                        unfocusedBorderColor = TextSecondary,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        errorBorderColor = Color.Red
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("newsletter_email_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (emailInput.isBlank()) {
                            emailError = "Email cannot be empty"
                        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
                            emailError = "Please enter a valid email address"
                        } else {
                            onSubscribe(emailInput)
                            emailInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonTeal, contentColor = SpaceDark),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("newsletter_subscribe_button")
                ) {
                    Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Subscribe to Ingest News", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(SuccessGreen.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Subscribed Icon",
                        tint = SuccessGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "You're on the list! 🚀",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "We'll send real-time streaming platform integration dispatches to:",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = subscribedEmail,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonTeal,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        onUnsubscribe()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CarbonLight, contentColor = TextSecondary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .testTag("newsletter_unsubscribe_button")
                ) {
                    Text("Unsubscribe", fontSize = 11.sp)
                }
            }
        }
    }
}


// ==========================================
// GOOGLE MUSIC TRENDS & INDUSTRY SEARCH WIDGET
// ==========================================
@Composable
fun GoogleMusicTrendsSearchWidget(
    searchUiState: SearchUiState,
    onSearch: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val trendChips = listOf(
        "Spotify Royalties",
        "TikTok Deals",
        "Dolby Atmos",
        "YouTube Ingestion",
        "DDEX Standard"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("google_search_trends_card"),
        colors = CardDefaults.cardColors(containerColor = CarbonSlate),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, GlassBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(NeonTeal.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Icon",
                        tint = NeonTeal,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = "Google Music Trends Hub",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Real-time distribution news & streaming updates",
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Search input and button Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search platform updates...", fontSize = 11.sp, color = TextSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonTeal,
                        unfocusedBorderColor = TextSecondary.copy(alpha = 0.5f),
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    textStyle = TextStyle(fontSize = 12.sp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("trend_search_input")
                )

                Button(
                    onClick = { onSearch(searchQuery) },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonTeal, contentColor = SpaceDark),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("trend_search_submit_button")
                ) {
                    Text("Search", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Popular Trend Chips
            Text(
                text = "POPULAR INQUIRIES",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = NeonPink,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(trendChips) { chip ->
                    val isSelected = searchQuery.equals(chip, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .background(
                                if (isSelected) NeonTeal.copy(alpha = 0.2f) else CarbonLight,
                                RoundedCornerShape(8.dp)
                            )
                            .border(
                                1.dp,
                                if (isSelected) NeonTeal else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                searchQuery = chip
                                onSearch(chip)
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = chip,
                            fontSize = 9.sp,
                            color = if (isSelected) NeonTeal else TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(color = CarbonLight, thickness = 1.dp)

            Spacer(modifier = Modifier.height(12.dp))

            // Results Container
            when (searchUiState) {
                is SearchUiState.Idle -> {
                    Text(
                        text = "Enter a search query or select a popular inquiry above to analyze distribution trends.",
                        fontSize = 10.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    )
                }
                is SearchUiState.Loading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = NeonTeal,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Consulting Google news indices...",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }
                }
                is SearchUiState.Success -> {
                    val results = searchUiState.results
                    if (results.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No live updates matching \"$searchQuery\"",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Try searching with broader tags like 'Spotify', 'Royalty', or 'TikTok'.",
                                fontSize = 10.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "LIVE TRENDS & ANNOTATED UPDATES",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "${results.size} match${if (results.size > 1) "es" else ""}",
                                    fontSize = 9.sp,
                                    color = NeonTeal,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            results.forEach { result ->
                                TrendResultCard(result = result)
                            }
                        }
                    }
                }
                is SearchUiState.Error -> {
                    Text(
                        text = "Search Error: ${searchUiState.message}. Please try again later.",
                        fontSize = 11.sp,
                        color = Color.Red,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TrendResultCard(result: TrendResult) {
    var isExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { isExpanded = !isExpanded },
        colors = CardDefaults.cardColors(containerColor = SpaceDark),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, GlassBorder.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(NeonViolet.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = result.badge,
                        fontSize = 8.sp,
                        color = NeonTeal, // Contrast-safe primary color
                        fontWeight = FontWeight.Black
                    )
                }

                Text(
                    text = result.date,
                    fontSize = 8.sp,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = result.title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (isExpanded) result.content else result.content.take(70) + if (result.content.length > 70) "..." else "",
                fontSize = 10.sp,
                color = TextSecondary,
                lineHeight = 14.sp
            )

            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Source: ${result.source}",
                        fontSize = 8.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold
                    )

                    Box(
                        modifier = Modifier
                            .background(NeonTeal.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .clickable {
                                Toast
                                    .makeText(context, "Opening platform bulletin board...", Toast.LENGTH_SHORT)
                                    .show()
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Launch,
                                contentDescription = null,
                                tint = NeonTeal,
                                modifier = Modifier.size(8.dp)
                            )
                            Text(
                                text = "READ MORE",
                                fontSize = 8.sp,
                                color = NeonTeal,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}


// ==========================================
// ARTIST PROFILE COMPONENT HELPERS
// ==========================================
@Composable
fun ArtistProfileImage(imageType: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(
                Brush.sweepGradient(listOf(NeonTeal, NeonViolet, NeonPink, NeonTeal)),
                CircleShape
            )
            .padding(3.dp)
            .clip(CircleShape)
    ) {
        if (imageType.startsWith("content://") || imageType.startsWith("file://") || imageType.startsWith("http")) {
            AsyncImage(
                model = imageType,
                contentDescription = "Artist Photo",
                modifier = Modifier.fillMaxSize().clip(CircleShape),
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.img_app_icon_1782777870303),
                placeholder = painterResource(id = R.drawable.img_app_icon_1782777870303)
            )
        } else {
            val colorFilter = when (imageType) {
                "AVATAR_NEON" -> ColorFilter.tint(NeonTeal.copy(alpha = 0.2f), androidx.compose.ui.graphics.BlendMode.ColorBurn)
                "AVATAR_COSMIC" -> ColorFilter.tint(NeonViolet.copy(alpha = 0.25f), androidx.compose.ui.graphics.BlendMode.ColorBurn)
                "AVATAR_PINK" -> ColorFilter.tint(NeonPink.copy(alpha = 0.2f), androidx.compose.ui.graphics.BlendMode.ColorBurn)
                else -> null
            }

            Image(
                painter = painterResource(id = R.drawable.img_app_icon_1782777870303),
                contentDescription = "Avatar",
                modifier = Modifier.fillMaxSize().clip(CircleShape),
                contentScale = ContentScale.Crop,
                colorFilter = colorFilter
            )
        }
    }
}

@Composable
fun SocialLinkStatusRow(
    platformName: String,
    link: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(SpaceDark, RoundedCornerShape(8.dp))
            .border(1.dp, GlassBorder.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(12.dp))
            }
            Column {
                Text(text = platformName, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(
                    text = link.ifEmpty { "Not linked • Click to link" },
                    fontSize = 8.sp,
                    color = if (link.isEmpty()) AlertOrange else TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Box(
            modifier = Modifier
                .background(
                    if (link.isNotEmpty()) SuccessGreen.copy(alpha = 0.1f) else AlertOrange.copy(alpha = 0.1f),
                    RoundedCornerShape(6.dp)
                )
                .clickable {
                    if (link.isNotEmpty()) {
                        Toast.makeText(context, "Redirecting to verified artist channel...", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Edit your Artist Profile to link this account!", Toast.LENGTH_SHORT).show()
                    }
                }
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = if (link.isNotEmpty()) "LINKED" else "PENDING CLAIM",
                color = if (link.isNotEmpty()) SuccessGreen else AlertOrange,
                fontSize = 8.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}


// ==========================================
// PROFILE EDIT DIALOG
// ==========================================
@Composable
fun ProfileDialog(
    initialArtist: String,
    initialLabel: String,
    initialBio: String,
    initialSpotify: String,
    initialApple: String,
    initialInstagram: String,
    initialYoutube: String,
    initialImageType: String,
    initialEmailNotifications: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, String, String, String, Boolean) -> Unit
) {
    var name by remember { mutableStateOf(initialArtist) }
    var label by remember { mutableStateOf(initialLabel) }
    var bio by remember { mutableStateOf(initialBio) }
    var spotify by remember { mutableStateOf(initialSpotify) }
    var apple by remember { mutableStateOf(initialApple) }
    var instagram by remember { mutableStateOf(initialInstagram) }
    var youtube by remember { mutableStateOf(initialYoutube) }
    var imageType by remember { mutableStateOf(initialImageType) }
    var emailNotifications by remember { mutableStateOf(initialEmailNotifications) }

    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imageType = uri.toString()
            Toast.makeText(context, "Artist Photo uploaded successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Artist Profile", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Artist Profile Picture",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonTeal
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ArtistProfileImage(imageType = imageType, modifier = Modifier.size(72.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonViolet),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Upload Image", fontSize = 11.sp)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            val presets = listOf(
                                "DEFAULT" to "Base",
                                "AVATAR_NEON" to "Neon",
                                "AVATAR_COSMIC" to "Cosmic",
                                "AVATAR_PINK" to "Pink"
                            )
                            presets.forEach { (type, lbl) ->
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (imageType == type) NeonTeal.copy(alpha = 0.2f) else CarbonLight,
                                            RoundedCornerShape(6.dp)
                                        )
                                        .border(
                                            1.dp,
                                            if (imageType == type) NeonTeal else Color.Transparent,
                                            RoundedCornerShape(6.dp)
                                        )
                                        .clickable { imageType = type }
                                        .padding(horizontal = 6.dp, vertical = 4.dp)
                                ) {
                                    Text(text = lbl, fontSize = 8.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = CarbonLight, thickness = 1.dp)

                Text(
                    text = "Artist Identity",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonTeal
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Artist / Stage Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonTeal,
                        unfocusedBorderColor = TextSecondary,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("edit_profile_artist_name"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Default Record Label") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonTeal,
                        unfocusedBorderColor = TextSecondary,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("edit_profile_label"),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Artist Bio (${bio.length}/300)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonTeal
                    )

                    Box(
                        modifier = Modifier
                            .background(
                                Brush.horizontalGradient(listOf(NeonViolet, NeonPink)),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                if (name.isBlank()) {
                                    Toast.makeText(context, "Please enter your stage name first!", Toast.LENGTH_SHORT).show()
                                } else {
                                    val genres = listOf("indie folk", "fusion pop", "desi electronic", "ambient soul", "retro acoustic")
                                    val instruments = listOf("sitar and acoustic guitar", "synthesizers and flute", "tabla and electronic drum pads")
                                    val selectedGenre = genres.random()
                                    val selectedInstrument = instruments.random()
                                    
                                    bio = "Introducing $name, a ground-breaking independent artist weaving together raw local $selectedGenre sounds with international beats. Expressing personal stories through $selectedInstrument, $name is crafting the future of modern music."
                                    Toast.makeText(context, "✨ Bio Generated with AI!", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                            Text("AI Bio Gen", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                OutlinedTextField(
                    value = bio,
                    onValueChange = { if (it.length <= 300) bio = it },
                    label = { Text("Write about your musical journey") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonTeal,
                        unfocusedBorderColor = TextSecondary,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().height(100.dp).testTag("edit_profile_bio"),
                    maxLines = 4
                )

                HorizontalDivider(color = CarbonLight, thickness = 1.dp)

                Text(
                    text = "Streaming Platforms Metadata Linkages (Auto Claim)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonTeal
                )

                OutlinedTextField(
                    value = spotify,
                    onValueChange = { spotify = it },
                    label = { Text("Spotify Artist URL") },
                    placeholder = { Text("https://open.spotify.com/artist/...") },
                    leadingIcon = { Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color(0xFF1DB954)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonTeal,
                        unfocusedBorderColor = TextSecondary,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("edit_profile_spotify"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = apple,
                    onValueChange = { apple = it },
                    label = { Text("Apple Music Artist URL") },
                    placeholder = { Text("https://music.apple.com/artist/...") },
                    leadingIcon = { Icon(Icons.Default.Audiotrack, contentDescription = null, tint = Color(0xFFFC3C44)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonTeal,
                        unfocusedBorderColor = TextSecondary,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("edit_profile_apple"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = instagram,
                    onValueChange = { instagram = it },
                    label = { Text("Instagram Handle or URL") },
                    placeholder = { Text("@rajumusic_official") },
                    leadingIcon = { Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = Color(0xFFE1306C)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonTeal,
                        unfocusedBorderColor = TextSecondary,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("edit_profile_instagram"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = youtube,
                    onValueChange = { youtube = it },
                    label = { Text("YouTube Channel Link") },
                    placeholder = { Text("https://youtube.com/c/...") },
                    leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color(0xFFFF0000)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonTeal,
                        unfocusedBorderColor = TextSecondary,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("edit_profile_youtube"),
                    singleLine = true
                )

                HorizontalDivider(color = CarbonLight, thickness = 1.dp)

                Text(
                    text = "Notification Preferences",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonTeal
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CarbonLight, RoundedCornerShape(12.dp))
                        .clickable { emailNotifications = !emailNotifications }
                        .padding(12.dp)
                        .testTag("email_notifications_row"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Music Status Email Alerts",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Get notified instantly via email when your track goes live, gets delivered, or needs metadata corrections.",
                            fontSize = 9.sp,
                            color = TextSecondary,
                            lineHeight = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Switch(
                        checked = emailNotifications,
                        onCheckedChange = { emailNotifications = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = SpaceDark,
                            checkedTrackColor = NeonTeal,
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = CarbonSlate
                        ),
                        modifier = Modifier.testTag("email_notifications_switch")
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, label, bio, spotify, apple, instagram, youtube, imageType, emailNotifications) },
                colors = ButtonDefaults.buttonColors(containerColor = NeonTeal, contentColor = SpaceDark),
                modifier = Modifier.testTag("save_profile_button")
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = NeonPink)
            }
        },
        containerColor = CarbonSlate,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
    )
}

@Composable
fun OnboardingWalkthroughOverlay(
    step: Int,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onSkip: () -> Unit
) {
    val title = when (step) {
        0 -> "Welcome to ST Digital! 🚀"
        1 -> "Track Global Metrics on the Hub 📈"
        2 -> "Distribute Songs Instantly 💿"
        3 -> "Download Your Legal Agreement 📜"
        else -> "Customize Profiles & Alert Settings ⚙️"
    }

    val badge = when (step) {
        0 -> "WELCOME TOUR"
        1 -> "ARTIST HUB"
        2 -> "FREE DISTRIBUTION"
        3 -> "LEGAL PROTECTION"
        else -> "NOTIFICATIONS & PROFILE"
    }

    val description = when (step) {
        0 -> "ST Digital is your ultimate 100% commission-free global music distributor. Upload your songs, retain 100% of your royalties, and expand your reach across all major global streaming platforms."
        1 -> "Your central cockpit! View live daily streaming counters, pending royalties, and monitor status updates on your releases as they transition from 'Ingestion' to 'Live in Stores'."
        2 -> "Have a new masterpiece? Enter track metadata, composers, lyrics, and cover art under the 'Distribute' tab. We submit your tracks to Spotify, JioSaavn, and Apple Music within 24-48 hours!"
        3 -> "Security first! Under the 'Analytics' tab, view store-by-store comparison breakdowns and download your legally-binding, signed, 100% commission-free distribution certificate as a PDF."
        else -> "Stand out! Tap 'Edit Profile' to change your bio, select premium avatar images, and toggle our automated email notifications switch to receive status alerts on streaming platforms."
    }

    val icon = when (step) {
        0 -> Icons.Default.MusicNote
        1 -> Icons.Default.TrendingUp
        2 -> Icons.Default.CloudUpload
        3 -> Icons.Default.Verified
        else -> Icons.Default.Settings
    }

    val hintText = when (step) {
        0 -> "Tip: Tap 'Tour Guide' anytime on your dashboard to review this helpful walkthrough."
        1 -> "Active View: Reviewing your central statistics dashboard page right now."
        2 -> "Active View: Switched automatically to the 'Distribute' tab so you can preview the upload panel."
        3 -> "Active View: Switched automatically to 'Analytics' to review savings and PDF agreements."
        else -> "Tip: Open your profile settings to access notification preferences and edit social credentials."
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.82f))
            .clickable(enabled = false) {}
            .testTag("onboarding_overlay_container"),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp)
                .border(2.dp, Brush.linearGradient(listOf(NeonTeal, NeonPink)), RoundedCornerShape(24.dp))
                .testTag("onboarding_card"),
            colors = CardDefaults.cardColors(containerColor = CarbonSlate),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(NeonPink.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = badge,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = NeonPink
                        )
                    }

                    Text(
                        text = "Step ${step + 1} of 5",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                }

                // Beautiful glowing icon container
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(NeonTeal.copy(alpha = 0.2f), Color.Transparent)
                            ),
                            CircleShape
                        )
                        .border(1.5.dp, NeonTeal.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = "Step Icon",
                        tint = NeonTeal,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Title
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )

                // Description
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Interactive program hint
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SpaceDark.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Hint Info",
                        tint = NeonTeal,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = hintText,
                        fontSize = 10.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Progress Indicator Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0..4) {
                        Box(
                            modifier = Modifier
                                .size(if (i == step) 16.dp else 8.dp, 8.dp)
                                .clip(CircleShape)
                                .background(if (i == step) NeonTeal else CarbonLight)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Navigation Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (step > 0) {
                        TextButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("onboarding_back_button")
                        ) {
                            Text(
                                text = "BACK",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        TextButton(
                            onClick = onSkip,
                            modifier = Modifier.testTag("onboarding_skip_button")
                        ) {
                            Text(
                                text = "SKIP TOUR",
                                color = NeonPink,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Button(
                        onClick = onNext,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonTeal, contentColor = SpaceDark),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(40.dp)
                            .testTag("onboarding_next_button")
                    ) {
                        Text(
                            text = if (step == 4) "FINISH TOUR 🎯" else "NEXT STEP",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}
