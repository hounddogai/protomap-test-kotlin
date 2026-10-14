package com.acme.transport.terminal.traileryard.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedTrailerYardAuditStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedTrailerYardAuditStore()
        val item = newBatchedTrailerYardAudit("code-428", "label-294")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
