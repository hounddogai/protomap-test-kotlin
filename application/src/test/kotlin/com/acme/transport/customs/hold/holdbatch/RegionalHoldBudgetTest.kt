package com.acme.transport.customs.hold.holdbatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalHoldBudgetTest {
    @Test
    fun `starts in proposed`() {
        val item = newRegionalHoldBudget("code-217", "name-958")
        assertEquals(RegionalHoldBudgetStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newRegionalHoldBudget("code-217", "name-958")
        try {
            item.moveTo(RegionalHoldBudgetStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalHoldBudgetStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newRegionalHoldBudget("code-217", "name-958")
        item.status = RegionalHoldBudgetStatus.WITHDRAWN
        assertFails("move") { item.moveTo(RegionalHoldBudgetStatus.PROPOSED) }
    }
}
