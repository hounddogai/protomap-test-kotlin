package com.acme.transport.safety.nearmiss.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledNearMissBudgetTest {
    @Test
    fun `starts in open`() {
        val item = newScheduledNearMissBudget("code-812", "title-633")
        assertEquals(ScheduledNearMissBudgetStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newScheduledNearMissBudget("code-812", "title-633")
        try {
            item.moveTo(ScheduledNearMissBudgetStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledNearMissBudgetStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newScheduledNearMissBudget("code-812", "title-633")
        item.status = ScheduledNearMissBudgetStatus.ARCHIVED
        assertFails("move") { item.moveTo(ScheduledNearMissBudgetStatus.OPEN) }
    }
}
