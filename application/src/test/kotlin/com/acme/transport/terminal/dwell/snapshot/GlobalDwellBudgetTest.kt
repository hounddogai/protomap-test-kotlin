package com.acme.transport.terminal.dwell.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalDwellBudgetTest {
    @Test
    fun `starts in idle`() {
        val item = newGlobalDwellBudget("code-937", "label-480")
        assertEquals(GlobalDwellBudgetStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newGlobalDwellBudget("code-937", "label-480")
        try {
            item.moveTo(GlobalDwellBudgetStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalDwellBudgetStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newGlobalDwellBudget("code-937", "label-480")
        item.status = GlobalDwellBudgetStatus.STOPPED
        assertFails("move") { item.moveTo(GlobalDwellBudgetStatus.IDLE) }
    }
}
