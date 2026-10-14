package com.acme.transport.freightpay.accessorial.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledAccessorialBudgetTest {
    @Test
    fun `starts in requested`() {
        val item = newScheduledAccessorialBudget("code-7", "title-276")
        assertEquals(ScheduledAccessorialBudgetStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newScheduledAccessorialBudget("code-7", "title-276")
        try {
            item.moveTo(ScheduledAccessorialBudgetStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledAccessorialBudgetStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newScheduledAccessorialBudget("code-7", "title-276")
        item.status = ScheduledAccessorialBudgetStatus.EXPIRED
        assertFails("move") { item.moveTo(ScheduledAccessorialBudgetStatus.REQUESTED) }
    }
}
