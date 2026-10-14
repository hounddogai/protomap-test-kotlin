package com.acme.transport.safety.violation.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingViolationBudgetTest {
    @Test
    fun `starts in proposed`() {
        val item = newRollingViolationBudget("code-780", "title-20")
        assertEquals(RollingViolationBudgetStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newRollingViolationBudget("code-780", "title-20")
        try {
            item.moveTo(RollingViolationBudgetStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingViolationBudgetStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newRollingViolationBudget("code-780", "title-20")
        item.status = RollingViolationBudgetStatus.WITHDRAWN
        assertFails("move") { item.moveTo(RollingViolationBudgetStatus.PROPOSED) }
    }
}
