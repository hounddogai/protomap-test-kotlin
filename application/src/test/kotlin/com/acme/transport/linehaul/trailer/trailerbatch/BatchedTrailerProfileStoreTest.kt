package com.acme.transport.linehaul.trailer.trailerbatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedTrailerProfileStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedTrailerProfileStore()
        val item = newBatchedTrailerProfile("code-516", "label-657")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
