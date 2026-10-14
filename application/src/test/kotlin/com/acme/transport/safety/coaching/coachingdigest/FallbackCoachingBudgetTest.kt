package com.acme.transport.safety.coaching.coachingdigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackCoachingBudgetTest {
    @Test
    fun `starts in idle`() {
        val item = newFallbackCoachingBudget("code-602", "label-334")
        assertEquals(FallbackCoachingBudgetStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newFallbackCoachingBudget("code-602", "label-334")
        try {
            item.moveTo(FallbackCoachingBudgetStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackCoachingBudgetStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newFallbackCoachingBudget("code-602", "label-334")
        item.status = FallbackCoachingBudgetStatus.STOPPED
        assertFails("move") { item.moveTo(FallbackCoachingBudgetStatus.IDLE) }
    }
}
