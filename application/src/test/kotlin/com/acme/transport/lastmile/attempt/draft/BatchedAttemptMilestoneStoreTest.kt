package com.acme.transport.lastmile.attempt.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedAttemptMilestoneStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedAttemptMilestoneStore()
        val item = newBatchedAttemptMilestone("code-344", "name-434")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
