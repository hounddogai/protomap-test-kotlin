package com.acme.transport.lastmile.locker.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LockerBudgetTest {
    @Test
    fun `starts in created`() {
        val item = newLockerBudget("code-329", "name-92")
        assertEquals(LockerBudgetStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newLockerBudget("code-329", "name-92")
        try {
            item.moveTo(LockerBudgetStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LockerBudgetStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newLockerBudget("code-329", "name-92")
        item.status = LockerBudgetStatus.RETIRED
        assertFails("move") { item.moveTo(LockerBudgetStatus.CREATED) }
    }
}
