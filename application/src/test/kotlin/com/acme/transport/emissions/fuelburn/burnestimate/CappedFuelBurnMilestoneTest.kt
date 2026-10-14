package com.acme.transport.emissions.fuelburn.burnestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedFuelBurnMilestoneTest {
    @Test
    fun `starts in open`() {
        val item = newCappedFuelBurnMilestone("code-249", "title-598")
        assertEquals(CappedFuelBurnMilestoneStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newCappedFuelBurnMilestone("code-249", "title-598")
        try {
            item.moveTo(CappedFuelBurnMilestoneStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedFuelBurnMilestoneStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newCappedFuelBurnMilestone("code-249", "title-598")
        item.status = CappedFuelBurnMilestoneStatus.ARCHIVED
        assertFails("move") { item.moveTo(CappedFuelBurnMilestoneStatus.OPEN) }
    }
}
