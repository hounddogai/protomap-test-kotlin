package com.acme.transport.crewing.restbreak.breakmilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledRestBreakMilestoneStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledRestBreakMilestoneStore()
        val item = newScheduledRestBreakMilestone("code-927", "name-827")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
