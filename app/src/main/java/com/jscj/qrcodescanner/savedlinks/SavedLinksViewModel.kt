package com.jscj.qrcodescanner.savedlinks

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SavedLinksViewModel(context: Context): ViewModel() {
    // LiveData to hold the list of saved links
    private val _savedLinks = MutableLiveData<List<String>>(emptyList())
    val savedLinks: LiveData<List<String>> = _savedLinks

    // Function to add a new link
    fun addLink(newLink: String) {
        if (newLink.isNotEmpty()) {
            val updatedList = _savedLinks.value.orEmpty() + newLink
            _savedLinks.value = updatedList
            // Save the updated list to persistent storage
        }
    }

    // Function to load saved links from persistent storage
    fun loadSavedLinks() {
        // Load the links from SharedPreferences or another storage solution
        // _savedLinks.value = loadedLinks
    }

    init {
        loadSavedLinks()
    }
}
