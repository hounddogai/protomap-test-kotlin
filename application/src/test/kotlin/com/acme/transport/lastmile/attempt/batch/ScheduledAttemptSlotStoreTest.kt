package com.acme.transport.lastmile.attempt.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledAttemptSlotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledAttemptSlotStore()
        val item = newScheduledAttemptSlot("code-201", "label-255")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
