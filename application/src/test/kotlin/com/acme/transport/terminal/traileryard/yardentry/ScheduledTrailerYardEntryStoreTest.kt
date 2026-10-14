package com.acme.transport.terminal.traileryard.yardentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledTrailerYardEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledTrailerYardEntryStore()
        val item = newScheduledTrailerYardEntry("code-728", "name-713")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
