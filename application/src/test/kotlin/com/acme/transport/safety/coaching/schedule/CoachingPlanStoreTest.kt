package com.acme.transport.safety.coaching.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CoachingPlanStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCoachingPlanStore()
        val item = newCoachingPlan("code-846", "name-160")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
