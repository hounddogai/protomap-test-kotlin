package com.acme.transport.terminal.dwell.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DwellBudgetTest {
    @Test
    fun `starts in requested`() {
        val item = newDwellBudget("code-678", "name-879")
        assertEquals(DwellBudgetStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newDwellBudget("code-678", "name-879")
        try {
            item.moveTo(DwellBudgetStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DwellBudgetStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newDwellBudget("code-678", "name-879")
        item.status = DwellBudgetStatus.EXPIRED
        assertFails("move") { item.moveTo(DwellBudgetStatus.REQUESTED) }
    }
}
