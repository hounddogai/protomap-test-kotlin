package com.acme.transport.terminal.dockdoor.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DockDoorBudgetTest {
    @Test
    fun `starts in planned`() {
        val item = newDockDoorBudget("code-530", "title-787")
        assertEquals(DockDoorBudgetStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newDockDoorBudget("code-530", "title-787")
        try {
            item.moveTo(DockDoorBudgetStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DockDoorBudgetStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newDockDoorBudget("code-530", "title-787")
        item.status = DockDoorBudgetStatus.CANCELLED
        assertFails("move") { item.moveTo(DockDoorBudgetStatus.PLANNED) }
    }
}
