package com.acme.transport.terminal.traileryard.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedTrailerYardEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedTrailerYardEntryStore()
        val item = newWeightedTrailerYardEntry("code-454", "label-788")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
