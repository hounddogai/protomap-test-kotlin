package com.acme.transport.lastmile.drop.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalDropBudgetTest {
    @Test
    fun `starts in open`() {
        val item = newSeasonalDropBudget("code-989", "title-353")
        assertEquals(SeasonalDropBudgetStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newSeasonalDropBudget("code-989", "title-353")
        try {
            item.moveTo(SeasonalDropBudgetStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalDropBudgetStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newSeasonalDropBudget("code-989", "title-353")
        item.status = SeasonalDropBudgetStatus.ARCHIVED
        assertFails("move") { item.moveTo(SeasonalDropBudgetStatus.OPEN) }
    }
}
