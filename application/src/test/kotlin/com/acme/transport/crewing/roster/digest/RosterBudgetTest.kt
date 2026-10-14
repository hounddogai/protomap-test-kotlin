package com.acme.transport.crewing.roster.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RosterBudgetTest {
    @Test
    fun `starts in planned`() {
        val item = newRosterBudget("code-264", "title-508")
        assertEquals(RosterBudgetStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newRosterBudget("code-264", "title-508")
        try {
            item.moveTo(RosterBudgetStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RosterBudgetStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newRosterBudget("code-264", "title-508")
        item.status = RosterBudgetStatus.CANCELLED
        assertFails("move") { item.moveTo(RosterBudgetStatus.PLANNED) }
    }
}
