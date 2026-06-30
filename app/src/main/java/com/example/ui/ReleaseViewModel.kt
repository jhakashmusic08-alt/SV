package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.Release
import com.example.data.ReleaseRepository
import com.example.data.RecentActivity
import android.content.Context
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import org.json.JSONArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class SearchUiState {
    object Idle : SearchUiState()
    object Loading : SearchUiState()
    data class Success(val results: List<TrendResult>) : SearchUiState()
    data class Error(val message: String) : SearchUiState()
}

data class TrendResult(
    val title: String,
    val source: String,
    val date: String,
    val content: String,
    val linkUrl: String,
    val badge: String
)

class ReleaseViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ReleaseRepository
    private val prefs = application.getSharedPreferences("artist_profile_prefs", Context.MODE_PRIVATE)

    val uiState: StateFlow<List<Release>>
    val recentActivities: StateFlow<List<RecentActivity>>

    // User Profile state
    private val _artistName = MutableStateFlow(prefs.getString("artist_name", "Artist Raju Indie") ?: "Artist Raju Indie")
    val artistName: StateFlow<String> = _artistName.asStateFlow()

    private val _recordLabel = MutableStateFlow(prefs.getString("record_label", "Raju Music Global") ?: "Raju Music Global")
    val recordLabel: StateFlow<String> = _recordLabel.asStateFlow()

    private val _artistBio = MutableStateFlow(
        prefs.getString(
            "artist_bio",
            "Independent artist crafting soulful indie music from India. Creating fusion melodies that blend traditional folk with modern ambient pop."
        ) ?: "Independent artist crafting soulful indie music from India. Creating fusion melodies that blend traditional folk with modern ambient pop."
    )
    val artistBio: StateFlow<String> = _artistBio.asStateFlow()

    private val _spotifyLink = MutableStateFlow(prefs.getString("spotify_link", "https://open.spotify.com/artist/4Z8W46vWhp7YfGWmclv67B") ?: "https://open.spotify.com/artist/4Z8W46vWhp7YfGWmclv67B")
    val spotifyLink: StateFlow<String> = _spotifyLink.asStateFlow()

    private val _appleMusicLink = MutableStateFlow(prefs.getString("apple_music_link", "https://music.apple.com/artist/raju-indie/17839082") ?: "https://music.apple.com/artist/raju-indie/17839082")
    val appleMusicLink: StateFlow<String> = _appleMusicLink.asStateFlow()

    private val _instagramLink = MutableStateFlow(prefs.getString("instagram_link", "https://instagram.com/rajumusic_official") ?: "https://instagram.com/rajumusic_official")
    val instagramLink: StateFlow<String> = _instagramLink.asStateFlow()

    private val _youtubeLink = MutableStateFlow(prefs.getString("youtube_link", "https://youtube.com/c/RajuIndieMusic") ?: "https://youtube.com/c/RajuIndieMusic")
    val youtubeLink: StateFlow<String> = _youtubeLink.asStateFlow()

    private val _artistImageType = MutableStateFlow(prefs.getString("artist_image_type", "DEFAULT") ?: "DEFAULT")
    val artistImageType: StateFlow<String> = _artistImageType.asStateFlow()

    private val _totalStreams = MutableStateFlow(128450L)
    val totalStreams: StateFlow<Long> = _totalStreams.asStateFlow()

    private val _totalEarnings = MutableStateFlow(3211.25)
    val totalEarnings: StateFlow<Double> = _totalEarnings.asStateFlow()

    private val _newsletterEmail = MutableStateFlow(prefs.getString("newsletter_email", "") ?: "")
    val newsletterEmail: StateFlow<String> = _newsletterEmail.asStateFlow()

    private val _isSubscribed = MutableStateFlow(prefs.getBoolean("is_subscribed", false))
    val isSubscribed: StateFlow<Boolean> = _isSubscribed.asStateFlow()

    private val _emailNotificationsEnabled = MutableStateFlow(prefs.getBoolean("email_notifications_enabled", true))
    val emailNotificationsEnabled: StateFlow<Boolean> = _emailNotificationsEnabled.asStateFlow()

    private val _onboardingCompleted = MutableStateFlow(prefs.getBoolean("onboarding_completed", false))
    val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted.asStateFlow()

    fun setOnboardingCompleted(completed: Boolean) {
        _onboardingCompleted.value = completed
        prefs.edit().putBoolean("onboarding_completed", completed).apply()
    }

    val fallbackTrends = listOf(
        TrendResult(
            title = "Spotify Payout Policy Updates for 2026",
            source = "Spotify for Artists",
            date = "June 15, 2026",
            content = "Spotify has increased stream threshold requirements and introduced stiffer penalties for artificial streaming. Artists must secure at least 1,000 unique listeners per release to qualify for direct royalties.",
            linkUrl = "https://artists.spotify.com",
            badge = "SPOTIFY"
        ),
        TrendResult(
            title = "TikTok Secures Multi-Year Deals for Indie Distributors",
            source = "Music Business Worldwide",
            date = "June 28, 2026",
            content = "TikTok has finalized global direct licensing agreements with independent music aggregators. These deals include automated metadata tagging and express ingestion pathways for viral sound trends.",
            linkUrl = "https://newsroom.tiktok.com",
            badge = "TIKTOK"
        ),
        TrendResult(
            title = "Apple Music Mandates Spatial Audio (Dolby Atmos)",
            source = "Apple Newsroom",
            date = "June 10, 2026",
            content = "All submissions to flagship playlists must include Dolby Atmos-mixed masters. Tracks mixed in spatial formats are receiving 100% royalty weightings compared to standard stereo tracks.",
            linkUrl = "https://music.apple.com",
            badge = "APPLE MUSIC"
        ),
        TrendResult(
            title = "YouTube Shorts Speeds Up DDEX Delivery Ingestion",
            source = "Digital Music News",
            date = "May 22, 2026",
            content = "YouTube has shortened DDEX ingestion windows for Shorts to under 1 hour. This helps independent artists publish and monetize audio tracks instantly as memes and trends arise.",
            linkUrl = "https://youtube.com",
            badge = "YOUTUBE"
        ),
        TrendResult(
            title = "Global Distribution Shifts to DDEX Ingestion Standard 4.3",
            source = "DDEX Association",
            date = "April 18, 2026",
            content = "DDEX standard 4.3 has been globally adopted, preserving lyric synchronization and rich metadata mapping for independent artists across over 150 streaming stores.",
            linkUrl = "https://ddex.net",
            badge = "METADATA"
        ),
        TrendResult(
            title = "Tidal Upgrades HiRes FLAC Support for Independent Uploaders",
            source = "Tidal News",
            date = "March 12, 2026",
            content = "Tidal is rolling out native support for 24-bit 192kHz HiRes FLAC files across all tier levels, encouraging independent artists to upload high-fidelity audio direct.",
            linkUrl = "https://tidal.com",
            badge = "TIDAL"
        ),
        TrendResult(
            title = "AI Metadata Ingestion Streamlines Global Licensing Rights",
            source = "Billboard Pro",
            date = "June 05, 2026",
            content = "Publishing registries are deploying AI-based verification to cross-reference global ISRCs and ISWNs, significantly reducing unclaimed royalty pools for indie singer-songwriters.",
            badge = "ALGORITHM",
            linkUrl = "https://www.billboard.com"
        ),
        TrendResult(
            title = "Royalty Payout Speeds Doubled for Direct API Partners",
            source = "Music Distribution Summit",
            date = "January 14, 2026",
            content = "Major independent distributors are transitioning to near-instant automated payouts, cutting standard 30-day royalty cycles to less than 15 days.",
            badge = "FINANCE",
            linkUrl = "https://musicdistribution.com"
        )
    )

    private val _searchUiState = MutableStateFlow<SearchUiState>(SearchUiState.Success(fallbackTrends.take(4)))
    val searchUiState: StateFlow<SearchUiState> = _searchUiState.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ReleaseRepository(database.releaseDao(), database.recentActivityDao())
        uiState = repository.allReleases.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        recentActivities = repository.recentActivities.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Seed some sample mock releases if database is empty on start
        viewModelScope.launch {
            delay(500)
            if (uiState.value.isEmpty()) {
                seedInitialData()
            }
        }

        // Simulate periodic stream/earning growth
        viewModelScope.launch {
            while (true) {
                delay(10000) // update every 10s
                val activeReleasesCount = uiState.value.count { it.status == "Live in Stores" }
                if (activeReleasesCount > 0) {
                    val streamGain = activeReleasesCount * Random.nextLong(15, 60)
                    _totalStreams.value += streamGain
                    _totalEarnings.value += streamGain * 0.12 // Rs 0.12 per stream (approx)
                }
            }
        }
    }

    private suspend fun seedInitialData() {
        val sample1 = Release(
            title = "Pyaar Ka Safar",
            artistName = "Artist Raju Indie",
            genre = "Romantic Pop",
            language = "Hindi",
            releaseDate = "2026-06-15",
            recordLabel = "Raju Music Global",
            upcCode = "UPC908127391",
            isrcCode = "IN-ST1-26-00041",
            status = "Live in Stores"
        )
        val sample2 = Release(
            title = "Bhojpuri Desi Beats",
            artistName = "Artist Raju Indie",
            featuringArtist = "DJ Wave",
            genre = "Folk / Bhojpuri",
            language = "Bhojpuri",
            releaseDate = "2026-06-25",
            recordLabel = "Raju Music Global",
            upcCode = "UPC908127392",
            isrcCode = "IN-ST1-26-00042",
            status = "Delivered to Stores"
        )
        repository.insert(sample1)
        repository.insert(sample2)

        // Seed realistic recent activities
        repository.insertActivity(RecentActivity(type = "PROFILE_CHANGE", title = "Account Created", description = "Successfully registered with ST Digital distribution platform.", timestamp = System.currentTimeMillis() - 86400000 * 3))
        repository.insertActivity(RecentActivity(type = "METADATA_UPDATE", title = "Contract Signed", description = "Indie free distribution agreement signed for Raju Indie.", timestamp = System.currentTimeMillis() - 86400000 * 2))
        repository.insertActivity(RecentActivity(type = "UPLOAD_COMPLETE", title = "Pyaar Ka Safar", description = "Uploaded Pyaar Ka Safar single to database.", timestamp = System.currentTimeMillis() - 3600000 * 5))
        repository.insertActivity(RecentActivity(type = "METADATA_UPDATE", title = "Metadata Approved", description = "Metadata check passed for Pyaar Ka Safar. Free ISRC IN-ST1-26-00041 assigned.", timestamp = System.currentTimeMillis() - 3600000 * 4))
        repository.insertActivity(RecentActivity(type = "METADATA_UPDATE", title = "Delivered to Stores", description = "Pyaar Ka Safar successfully dispatched to Spotify, JioSaavn, and Apple Music.", timestamp = System.currentTimeMillis() - 3600000 * 3))
        repository.insertActivity(RecentActivity(type = "METADATA_UPDATE", title = "Release Live", description = "Pyaar Ka Safar is now live in global stores!", timestamp = System.currentTimeMillis() - 3600000 * 2))
        repository.insertActivity(RecentActivity(type = "UPLOAD_COMPLETE", title = "Bhojpuri Desi Beats", description = "Uploaded Bhojpuri Desi Beats single to database.", timestamp = System.currentTimeMillis() - 1800000))
    }

    fun updateProfile(
        name: String,
        label: String,
        bio: String,
        spotify: String,
        apple: String,
        instagram: String,
        youtube: String,
        imageType: String,
        emailNotifications: Boolean
    ) {
        _artistName.value = name
        _recordLabel.value = label
        _artistBio.value = bio
        _spotifyLink.value = spotify
        _appleMusicLink.value = apple
        _instagramLink.value = instagram
        _youtubeLink.value = youtube
        _artistImageType.value = imageType
        _emailNotificationsEnabled.value = emailNotifications

        prefs.edit().apply {
            putString("artist_name", name)
            putString("record_label", label)
            putString("artist_bio", bio)
            putString("spotify_link", spotify)
            putString("apple_music_link", apple)
            putString("instagram_link", instagram)
            putString("youtube_link", youtube)
            putString("artist_image_type", imageType)
            putBoolean("email_notifications_enabled", emailNotifications)
            apply()
        }

        viewModelScope.launch {
            repository.insertActivity(RecentActivity(
                type = "PROFILE_CHANGE",
                title = "Profile Updated",
                description = "Artist profile details updated for $name. Bio, social links, and notifications configured."
            ))
        }
    }

    fun createRelease(
        title: String,
        artist: String,
        featuring: String,
        lyricist: String,
        composer: String,
        genre: String,
        language: String,
        releaseDate: String,
        label: String
    ) {
        viewModelScope.launch {
            // Auto generate standard music registration codes for high-professional fidelity
            val randUpc = "UPC" + Random.nextLong(100000000, 999999999).toString()
            val randIsrc = "IN-ST1-26-" + Random.nextInt(10000, 99999).toString()

            val newRelease = Release(
                title = title,
                artistName = artist,
                featuringArtist = featuring,
                lyricist = lyricist,
                composer = composer,
                genre = genre,
                language = language,
                releaseDate = releaseDate,
                recordLabel = label,
                upcCode = randUpc,
                isrcCode = randIsrc,
                status = "Pending Review"
            )

            val insertedId = repository.insert(newRelease).toInt()

            repository.insertActivity(RecentActivity(
                type = "UPLOAD_COMPLETE",
                title = "Upload Completed: $title",
                description = "Successfully uploaded track '$title' with UPC $randUpc and ISRC $randIsrc."
            ))

            // Launch automatic simulation background process to advance release status
            simulateReleaseJourney(insertedId)
        }
    }

    private fun simulateReleaseJourney(releaseId: Int) {
        viewModelScope.launch {
            val release = repository.getReleaseById(releaseId)
            val title = release?.title ?: "New Release"

            // 1. Pending -> Approved
            delay(12000) // 12 seconds
            repository.updateStatus(releaseId, "Approved")
            repository.insertActivity(RecentActivity(
                type = "METADATA_UPDATE",
                title = "Metadata Approved: $title",
                description = "Metadata check passed. Your release '$title' is approved for global distribution."
            ))

            // 2. Approved -> Delivered to Stores
            delay(12000) // 12 seconds
            repository.updateStatus(releaseId, "Delivered to Stores")
            repository.insertActivity(RecentActivity(
                type = "METADATA_UPDATE",
                title = "Delivered to Stores: $title",
                description = "Your release '$title' has been successfully delivered to major stores."
            ))

            // 3. Delivered -> Live in Stores
            delay(12000) // 12 seconds
            repository.updateStatus(releaseId, "Live in Stores")
            repository.insertActivity(RecentActivity(
                type = "METADATA_UPDATE",
                title = "Release Live: $title",
                description = "Your release '$title' is now live! Streams and royalties are now tracking in real-time."
            ))
        }
    }

    fun deleteRelease(release: Release) {
        viewModelScope.launch {
            repository.delete(release)
        }
    }

    fun subscribeNewsletter(email: String) {
        _newsletterEmail.value = email
        _isSubscribed.value = true
        prefs.edit().apply {
            putString("newsletter_email", email)
            putBoolean("is_subscribed", true)
            apply()
        }
    }

    fun unsubscribeNewsletter() {
        _newsletterEmail.value = ""
        _isSubscribed.value = false
        prefs.edit().apply {
            putString("newsletter_email", "")
            putBoolean("is_subscribed", false)
            apply()
        }
    }

    fun performTrendSearch(query: String) {
        viewModelScope.launch {
            if (query.trim().isEmpty()) {
                _searchUiState.value = SearchUiState.Success(fallbackTrends.take(4))
                return@launch
            }

            _searchUiState.value = SearchUiState.Loading

            // Attempt to get the Gemini API Key
            val apiKey = try {
                com.example.BuildConfig.GEMINI_API_KEY
            } catch (e: Exception) {
                ""
            }

            if (apiKey.isNotEmpty() && apiKey != "MY_GEMINI_API_KEY") {
                try {
                    val resultList = queryGeminiForTrends(query, apiKey)
                    if (resultList.isNotEmpty()) {
                        _searchUiState.value = SearchUiState.Success(resultList)
                        return@launch
                    }
                } catch (e: Exception) {
                    // Fallback on failure
                }
            }

            // Fallback: search and filter local trends
            delay(1000) // Mock search lag for realistic experience
            val filtered = fallbackTrends.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.content.contains(query, ignoreCase = true) ||
                        it.badge.contains(query, ignoreCase = true) ||
                        it.source.contains(query, ignoreCase = true)
            }
            _searchUiState.value = SearchUiState.Success(filtered)
        }
    }

    private suspend fun queryGeminiForTrends(query: String, apiKey: String): List<TrendResult> = withContext(Dispatchers.IO) {
        val client = OkHttpClient.Builder()
            .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .build()

        val prompt = """
            You are a music industry trend and platform news service. The artist is searching for: '$query'.
            Provide 3 realistic, highly detailed news items or trending updates related to this search query for music distribution and streaming platforms (like Spotify, Apple Music, YouTube, TikTok, Tidal, Deezer, global ingestion, DDEX standards) in 2026.
            Return a JSON array of objects. Do NOT use markdown code blocks or any other formatting. Only return the JSON.
            Each object in the array MUST have the following structure:
            {
              "title": "Headline of the update",
              "source": "Realistic publisher, e.g. Billboard, Spotify for Artists, MBW, Digital Music News, Tidal News",
              "date": "June 2026 or another realistic 2026 date",
              "content": "A detailed 2-3 sentence summary of the news including specific platform details, rates, or ingestion policies.",
              "linkUrl": "https://example.com/trends",
              "badge": "A short category badge like SPOTIFY, TIKTOK, ALGORITHM, METADATA, ROYALTY, DISTRIBUTION"
            }
        """.trimIndent()

        val jsonRequest = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            })
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply {
                        put("text", "You are an API endpoint that returns strictly valid JSON arrays of music industry trends. Do not wrap in markdown or add notes.")
                    })
                })
            })
        }

        val requestBody = jsonRequest.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) return@withContext emptyList()

        val responseBody = response.body?.string() ?: return@withContext emptyList()
        val responseJson = JSONObject(responseBody)
        val candidates = responseJson.getJSONArray("candidates")
        val contentObj = candidates.getJSONObject(0).getJSONObject("content")
        val parts = contentObj.getJSONArray("parts")
        val textResult = parts.getJSONObject(0).getString("text").trim()

        val cleanText = textResult.removePrefix("```json").removePrefix("```").removeSuffix("```").trim()

        val jsonArray = JSONArray(cleanText)
        val list = mutableListOf<TrendResult>()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            list.add(
                TrendResult(
                    title = obj.optString("title", "Industry Update"),
                    source = obj.optString("source", "Music Search"),
                    date = obj.optString("date", "June 2026"),
                    content = obj.optString("content", "Details of music distribution updates..."),
                    linkUrl = obj.optString("linkUrl", "https://example.com"),
                    badge = obj.optString("badge", "UPDATE").uppercase()
                )
            )
        }
        list
    }
}
