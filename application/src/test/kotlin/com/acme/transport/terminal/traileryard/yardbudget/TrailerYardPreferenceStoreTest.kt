package com.acme.transport.terminal.traileryard.yardbudget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TrailerYardPreferenceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTrailerYardPreferenceStore()
        val item = newTrailerYardPreference("code-80", "label-778")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
