package com.acme.transport.customs.clearance.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ClearanceBudgetTest {
    @Test
    fun `starts in open`() {
        val item = newClearanceBudget("code-60", "name-662")
        assertEquals(ClearanceBudgetStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newClearanceBudget("code-60", "name-662")
        try {
            item.moveTo(ClearanceBudgetStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ClearanceBudgetStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newClearanceBudget("code-60", "name-662")
        item.status = ClearanceBudgetStatus.ARCHIVED
        assertFails("move") { item.moveTo(ClearanceBudgetStatus.OPEN) }
    }
}
