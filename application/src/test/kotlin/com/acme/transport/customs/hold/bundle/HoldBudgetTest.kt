package com.acme.transport.customs.hold.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HoldBudgetTest {
    @Test
    fun `starts in created`() {
        val item = newHoldBudget("code-848", "name-452")
        assertEquals(HoldBudgetStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newHoldBudget("code-848", "name-452")
        try {
            item.moveTo(HoldBudgetStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HoldBudgetStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newHoldBudget("code-848", "name-452")
        item.status = HoldBudgetStatus.RETIRED
        assertFails("move") { item.moveTo(HoldBudgetStatus.CREATED) }
    }
}
