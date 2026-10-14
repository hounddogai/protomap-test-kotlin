package com.acme.transport.terminal.dwell.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultDwellBudgetTest {
    @Test
    fun `starts in idle`() {
        val item = newDefaultDwellBudget("code-960", "title-373")
        assertEquals(DefaultDwellBudgetStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newDefaultDwellBudget("code-960", "title-373")
        try {
            item.moveTo(DefaultDwellBudgetStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultDwellBudgetStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newDefaultDwellBudget("code-960", "title-373")
        item.status = DefaultDwellBudgetStatus.STOPPED
        assertFails("move") { item.moveTo(DefaultDwellBudgetStatus.IDLE) }
    }
}
