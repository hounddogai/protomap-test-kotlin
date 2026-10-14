package com.acme.transport.terminal.traileryard.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedTrailerYardPolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedTrailerYardPolicyStore()
        val item = newBatchedTrailerYardPolicy("code-425", "label-327")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
