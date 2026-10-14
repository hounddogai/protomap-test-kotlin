package com.acme.transport.terminal.traileryard.yardsummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TrailerYardCycleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTrailerYardCycleStore()
        val item = newTrailerYardCycle("code-437", "title-953")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
