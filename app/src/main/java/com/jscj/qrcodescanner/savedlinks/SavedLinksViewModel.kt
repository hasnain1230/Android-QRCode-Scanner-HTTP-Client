package com.jscj.qrcodescanner.savedlinks

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SavedLinksViewModel(context: Context) : ViewModel() {
    // LiveData to hold the list of saved links
    private val _sharedPreferences =
        context.getSharedPreferences("saved_links", Context.MODE_PRIVATE)
    private val _savedLinks = MutableLiveData<List<String>>(emptyList())
    val savedLinks: LiveData<List<String>> = _savedLinks

    init {
        loadSavedLinks()
    }

    // Function to add a new link
    fun addLink(newLink: String) {
        if (newLink.isNotEmpty()) {
            val updatedList = _savedLinks.value.orEmpty() + newLink
            _savedLinks.value = updatedList
            // Save the updated list to persistent storage
            with(_sharedPreferences.edit()) {
                putStringSet("saved_links", updatedList.toSet())
                apply() // This applies the changes to the SharedPreferences
            }
        }
    }

    fun removeLink(link: String) {
        val updatedList =
            _savedLinks.value.orEmpty() - link // The minus operator removes the link from the list
        _savedLinks.value = updatedList
        with(_sharedPreferences.edit()) {
            putStringSet("saved_links", updatedList.toSet())
            apply()
        }
    }

    // Function to remove list of links
    fun removeLinks(links: List<String>) {
        val updatedList = _savedLinks.value.orEmpty() - links.toSet()
        _savedLinks.value = updatedList
        with(_sharedPreferences.edit()) {
            putStringSet("saved_links", updatedList.toSet())
            apply()
        }
    }

    // Function to load saved links from persistent storage
    private fun loadSavedLinks() {
        // Load the links from SharedPreferences or another storage solution
        // _savedLinks.value = loadedLinks
        val loadedLinks = _sharedPreferences.getStringSet("saved_links", emptySet())
        _savedLinks.value = loadedLinks?.toList().orEmpty()
    }
}
