package com.acme.transport.safety.violation.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalViolationBudgetTest {
    @Test
    fun `starts in proposed`() {
        val item = newGlobalViolationBudget("code-662", "label-469")
        assertEquals(GlobalViolationBudgetStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newGlobalViolationBudget("code-662", "label-469")
        try {
            item.moveTo(GlobalViolationBudgetStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalViolationBudgetStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newGlobalViolationBudget("code-662", "label-469")
        item.status = GlobalViolationBudgetStatus.WITHDRAWN
        assertFails("move") { item.moveTo(GlobalViolationBudgetStatus.PROPOSED) }
    }
}
