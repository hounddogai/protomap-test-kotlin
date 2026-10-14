package com.acme.transport.lastmile.attempt.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackAttemptMilestoneStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackAttemptMilestoneStore()
        val item = newFallbackAttemptMilestone("code-535", "label-870")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
