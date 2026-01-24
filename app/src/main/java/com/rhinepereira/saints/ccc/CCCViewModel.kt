package com.rhinepereira.saints.ccc

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rhinepereira.saints.data.model.UiError
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class CCCState(
    val isLoading: Boolean = false,
    val entries: List<CCCEntry> = emptyList(),
    val filteredEntries: List<CCCEntry> = emptyList(),
    val error: UiError? = null
)

class CCCViewModel(private val context: Context) : ViewModel() {

    private val _uiState = MutableStateFlow(CCCState())
    val uiState: StateFlow<CCCState> = _uiState.asStateFlow()

    init {
        loadCCC()
    }

    fun loadCCC() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val jsonString = withContext(Dispatchers.IO) {
                    try {
                        context.assets.open("ccc.json").bufferedReader().use { it.readText() }
                    } catch (e: Exception) {
                        Log.e("CCCViewModel", "Error reading ccc.json", e)
                        throw e
                    }
                }
                
                val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                val listType = Types.newParameterizedType(List::class.java, CCCEntry::class.java)
                val adapter = moshi.adapter<List<CCCEntry>>(listType)
                
                val entries = adapter.fromJson(jsonString) ?: emptyList()
                Log.d("CCCViewModel", "Loaded ${entries.size} entries")
                
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        entries = entries,
                        filteredEntries = entries
                    ) 
                }
            } catch (e: Exception) {
                Log.e("CCCViewModel", "Failed to load CCC", e)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = UiError.Unknown(e.localizedMessage ?: e.message)
                    ) 
                }
            }
        }
    }

    fun search(query: String) {
        val entries = _uiState.value.entries
        if (query.isBlank()) {
            _uiState.update { it.copy(filteredEntries = entries) }
            return
        }

        val filtered = entries.filter { entry ->
            entry.id.toString().contains(query) || 
            entry.text.contains(query, ignoreCase = true)
        }
        _uiState.update { it.copy(filteredEntries = filtered) }
    }
}

class CCCViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CCCViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CCCViewModel(context.applicationContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
