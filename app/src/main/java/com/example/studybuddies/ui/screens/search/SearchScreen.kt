package com.example.studybuddies.ui.screens.search

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.studybuddies.data.model.User
import com.example.studybuddies.viewmodel.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel, // ViewModel managing search results and filter logic
    onTutorClick: (String) -> Unit // Navigation callback when a tutor is selected
) {
    // Observe the search state from the ViewModel
    val uiState by viewModel.uiState.collectAsState()
    val logoBlue = Color(0xFF1A73E8) // Primary branding color
    val lightBlue = Color(0xFFEBF2FF) // Background color for search inputs

    // Static lists for filter selection options
    val priceOptions = listOf("All prices", "Under 50 PLN", "50 - 100 PLN", "100+ PLN")
    val availabilityOptions = listOf("All", "Mornings", "Afternoons", "Evenings", "Weekends")

    // Temporary states to hold filter changes before user clicks "Apply"
    var tempPrice by remember { mutableStateOf(uiState.selectedPriceRange) }
    var tempMode by remember { mutableStateOf(uiState.selectedMode) }
    var tempAvailability by remember { mutableStateOf(uiState.selectedAvailability) }
    var tempLocation by remember { mutableStateOf("") }

    // Sync temporary filter states when the filter sheet is opened
    LaunchedEffect(uiState.showFilters) {
        if (uiState.showFilters) {
            tempPrice = uiState.selectedPriceRange
            tempMode = uiState.selectedMode
            tempAvailability = uiState.selectedAvailability
            tempLocation = if (uiState.selectedLocation == "All locations") "" else uiState.selectedLocation
        }
    }

    Scaffold(
        containerColor = Color.White,
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .statusBarsPadding() // Ensures content starts below the status bar
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp, bottom = 8.dp)
            ) {
                Text(
                    text = "Search for tutors",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = logoBlue,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Search bar for typing tutor names or subjects
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        modifier = Modifier.weight(1f).padding(end = 12.dp),
                        placeholder = { Text("Search subject or name", color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = logoBlue) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = logoBlue,
                            unfocusedBorderColor = logoBlue,
                            focusedContainerColor = lightBlue,
                            unfocusedContainerColor = lightBlue,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        ),
                        singleLine = true
                    )
                    // Button to toggle the advanced filter bottom sheet
                    FilledTonalIconButton(
                        onClick = { viewModel.toggleFilters() },
                        modifier = Modifier.size(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = lightBlue)
                    ) {
                        Icon(Icons.Default.Tune, null, tint = logoBlue)
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 16.dp,
                bottom = innerPadding.calculateBottomPadding() + 24.dp
            )
        ) {
            // Logic for "Recommended" tutors shown when no active search query exists
            val recommendedList = uiState.recentTutors.filter { it.uid != uiState.currentUserUid }

            if (uiState.searchQuery.isEmpty() && recommendedList.isNotEmpty()) {
                item {
                    Text(
                        text = "Recommended Tutors",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                items(recommendedList) { tutor ->
                    TutorSearchCard(tutor, onTutorClick)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF0F0F0))
                }
            }

            // Main filtered list logic, excluding the current user from results
            val listToShow = uiState.filteredTutors.filter { tutor ->
                tutor.uid != uiState.currentUserUid &&
                        (uiState.searchQuery.isNotEmpty() || recommendedList.none { it.uid == tutor.uid })
            }

            items(listToShow) { tutor ->
                TutorSearchCard(tutor, onTutorClick)
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Fallback message if the search yields no results
            if (listToShow.isEmpty() && uiState.searchQuery.isNotEmpty()) {
                item {
                    Text(
                        "No tutors found matching your criteria.",
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 20.dp)
                    )
                }
            }
        }

        // Modal Bottom Sheet for advanced filtering options
        if (uiState.showFilters) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.toggleFilters() },
                containerColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 24.dp)
                        .navigationBarsPadding() // Respects system navigation bar space
                        .verticalScroll(rememberScrollState())
                ) {
                    Text("Filters", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.padding(bottom = 24.dp))

                    // Price range selection dropdown
                    FilterDropdown("Price Range", tempPrice, priceOptions) { tempPrice = it }

                    Spacer(Modifier.height(16.dp))
                    Text("Mode", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                    Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("All", "Online", "In-person").forEach { mode ->
                            FilterChip(
                                selected = (tempMode == mode),
                                onClick = { tempMode = mode },
                                label = { Text(mode) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = logoBlue, selectedLabelColor = Color.White)
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))

                    // Availability selection dropdown
                    FilterDropdown("Availability", tempAvailability, availabilityOptions) { tempAvailability = it }

                    Spacer(Modifier.height(16.dp))
                    Text("Location", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                    // Input field for specific city/location filtering
                    OutlinedTextField(
                        value = tempLocation,
                        onValueChange = { tempLocation = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        placeholder = { Text("Type in the location") },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = logoBlue,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        ),
                        singleLine = true
                    )
                    Spacer(Modifier.height(32.dp))

                    // Button to submit the selected filters to the ViewModel
                    Button(
                        onClick = {
                            viewModel.updateFilters(
                                tempPrice,
                                tempMode,
                                tempAvailability,
                                if(tempLocation.trim().isEmpty()) "All locations" else tempLocation
                            )
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = logoBlue)
                    ) {
                        Text("Apply Filters", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDropdown(label: String, selected: String, options: List<String>, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) } // Controls dropdown visibility

    Column {
        Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black, modifier = Modifier.padding(bottom = 8.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selected,
                onValueChange = {},
                readOnly = true, // User selects from menu rather than typing
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.LightGray,
                    focusedBorderColor = Color(0xFF1A73E8),
                    unfocusedTextColor = Color.Black,
                    focusedTextColor = Color.Black
                )
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color.White)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, color = Color.Black) },
                        onClick = {
                            onSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TutorSearchCard(tutor: User, onClick: (String) -> Unit) {
    val logoBlue = Color(0xFF1A73E8)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick(tutor.uid) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, logoBlue.copy(alpha = 0.5f)), // Subtle blue border
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            // Profile image container with fallback to initials
            Box(
                modifier = Modifier.size(65.dp).clip(CircleShape).background(Color(0xFFF0F5FF)),
                contentAlignment = Alignment.Center
            ) {
                if (!tutor.profileImageUri.isNullOrEmpty()) {
                    AsyncImage(
                        model = tutor.profileImageUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(text = tutor.initials, color = logoBlue, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(tutor.fullName, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                Text(tutor.subjects.joinToString(" • "), color = Color.Gray, fontSize = 13.sp)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Icon(Icons.Default.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                    Text(
                        text = String.format("%.1f", if(tutor.averageRating == 0.0) 5.0 else tutor.averageRating),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(start = 4.dp, end = 12.dp)
                    )
                    Text(text = "${tutor.hourlyRate.toInt()} PLN/h", color = logoBlue, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}