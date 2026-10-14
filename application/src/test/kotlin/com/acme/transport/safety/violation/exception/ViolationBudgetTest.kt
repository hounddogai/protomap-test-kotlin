package com.acme.transport.safety.violation.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ViolationBudgetTest {
    @Test
    fun `starts in proposed`() {
        val item = newViolationBudget("code-67", "name-92")
        assertEquals(ViolationBudgetStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newViolationBudget("code-67", "name-92")
        try {
            item.moveTo(ViolationBudgetStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ViolationBudgetStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newViolationBudget("code-67", "name-92")
        item.status = ViolationBudgetStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ViolationBudgetStatus.PROPOSED) }
    }
}
