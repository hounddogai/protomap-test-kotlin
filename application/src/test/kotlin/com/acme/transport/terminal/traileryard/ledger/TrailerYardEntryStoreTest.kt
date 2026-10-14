package com.acme.transport.terminal.traileryard.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TrailerYardEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTrailerYardEntryStore()
        val item = newTrailerYardEntry("code-124", "name-624")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
