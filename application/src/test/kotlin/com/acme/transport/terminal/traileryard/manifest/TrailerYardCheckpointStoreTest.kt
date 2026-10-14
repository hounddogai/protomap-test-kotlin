package com.acme.transport.terminal.traileryard.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TrailerYardCheckpointStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTrailerYardCheckpointStore()
        val item = newTrailerYardCheckpoint("code-272", "label-141")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
