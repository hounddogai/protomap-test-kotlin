package com.acme.transport.lastmile.drop.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DropBudgetTest {
    @Test
    fun `starts in draft`() {
        val item = newDropBudget("code-177", "name-412")
        assertEquals(DropBudgetStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newDropBudget("code-177", "name-412")
        try {
            item.moveTo(DropBudgetStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DropBudgetStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newDropBudget("code-177", "name-412")
        item.status = DropBudgetStatus.CLOSED
        assertFails("move") { item.moveTo(DropBudgetStatus.DRAFT) }
    }
}
