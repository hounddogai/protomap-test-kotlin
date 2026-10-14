package com.acme.transport.terminal.traileryard.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TrailerYardDraftStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTrailerYardDraftStore()
        val item = newTrailerYardDraft("code-196", "title-165")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
