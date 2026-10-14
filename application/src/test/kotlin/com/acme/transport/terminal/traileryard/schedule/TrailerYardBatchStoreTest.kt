package com.acme.transport.terminal.traileryard.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TrailerYardBatchStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTrailerYardBatchStore()
        val item = newTrailerYardBatch("code-564", "name-548")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
