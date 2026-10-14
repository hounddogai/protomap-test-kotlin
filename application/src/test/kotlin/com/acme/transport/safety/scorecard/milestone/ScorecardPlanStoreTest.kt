package com.acme.transport.safety.scorecard.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScorecardPlanStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScorecardPlanStore()
        val item = newScorecardPlan("code-892", "label-342")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
