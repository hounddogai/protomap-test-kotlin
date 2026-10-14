package com.acme.transport.terminal.dwell.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalDwellPlanTest {
    @Test
    fun `starts in draft`() {
        val item = newRegionalDwellPlan("code-659", "title-638")
        assertEquals(RegionalDwellPlanStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newRegionalDwellPlan("code-659", "title-638")
        try {
            item.moveTo(RegionalDwellPlanStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalDwellPlanStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newRegionalDwellPlan("code-659", "title-638")
        item.status = RegionalDwellPlanStatus.CLOSED
        assertFails("move") { item.moveTo(RegionalDwellPlanStatus.DRAFT) }
    }
}
