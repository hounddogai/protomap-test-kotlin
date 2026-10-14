package com.acme.transport.terminal.traileryard.yardsummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredTrailerYardExceptionStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredTrailerYardExceptionStore()
        val item = newTieredTrailerYardException("code-715", "label-390")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
