package com.acme.transport.crewing.restbreak.breakrollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledRestBreakPlanStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledRestBreakPlanStore()
        val item = newScheduledRestBreakPlan("code-659", "title-53")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
