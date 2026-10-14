package com.acme.transport.terminal.crossdock.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CrossDockBudgetTest {
    @Test
    fun `starts in proposed`() {
        val item = newCrossDockBudget("code-199", "name-637")
        assertEquals(CrossDockBudgetStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newCrossDockBudget("code-199", "name-637")
        try {
            item.moveTo(CrossDockBudgetStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CrossDockBudgetStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newCrossDockBudget("code-199", "name-637")
        item.status = CrossDockBudgetStatus.WITHDRAWN
        assertFails("move") { item.moveTo(CrossDockBudgetStatus.PROPOSED) }
    }
}
