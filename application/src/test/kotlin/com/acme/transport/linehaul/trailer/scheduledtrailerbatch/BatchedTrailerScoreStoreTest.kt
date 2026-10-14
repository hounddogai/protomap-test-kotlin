package com.acme.transport.linehaul.trailer.scheduledtrailerbatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedTrailerScoreStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedTrailerScoreStore()
        val item = newBatchedTrailerScore("code-957", "label-279")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
