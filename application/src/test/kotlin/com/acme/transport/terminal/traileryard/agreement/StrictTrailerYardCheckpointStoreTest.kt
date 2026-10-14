package com.acme.transport.terminal.traileryard.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictTrailerYardCheckpointStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictTrailerYardCheckpointStore()
        val item = newStrictTrailerYardCheckpoint("code-125", "title-954")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
