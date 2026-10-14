package com.acme.transport.lastmile.attempt.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AttemptMilestoneStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAttemptMilestoneStore()
        val item = newAttemptMilestone("code-950", "title-223")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
