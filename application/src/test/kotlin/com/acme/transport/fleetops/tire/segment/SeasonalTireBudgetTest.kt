package com.acme.transport.fleetops.tire.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalTireBudgetTest {
    @Test
    fun `starts in draft`() {
        val item = newSeasonalTireBudget("code-396", "name-943")
        assertEquals(SeasonalTireBudgetStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newSeasonalTireBudget("code-396", "name-943")
        try {
            item.moveTo(SeasonalTireBudgetStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalTireBudgetStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newSeasonalTireBudget("code-396", "name-943")
        item.status = SeasonalTireBudgetStatus.CLOSED
        assertFails("move") { item.moveTo(SeasonalTireBudgetStatus.DRAFT) }
    }
}
