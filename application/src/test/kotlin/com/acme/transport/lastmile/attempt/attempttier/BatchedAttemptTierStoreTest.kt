package com.acme.transport.lastmile.attempt.attempttier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedAttemptTierStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedAttemptTierStore()
        val item = newBatchedAttemptTier("code-932", "label-271")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
