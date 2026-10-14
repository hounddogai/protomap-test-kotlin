package com.acme.transport.safety.coaching.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CoachingBudgetTest {
    @Test
    fun `starts in created`() {
        val item = newCoachingBudget("code-517", "label-415")
        assertEquals(CoachingBudgetStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newCoachingBudget("code-517", "label-415")
        try {
            item.moveTo(CoachingBudgetStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CoachingBudgetStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newCoachingBudget("code-517", "label-415")
        item.status = CoachingBudgetStatus.RETIRED
        assertFails("move") { item.moveTo(CoachingBudgetStatus.CREATED) }
    }
}
