package com.acme.transport.emissions.fuelburn.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackFuelBurnMilestoneTest {
    @Test
    fun `starts in open`() {
        val item = newFallbackFuelBurnMilestone("code-562", "title-382")
        assertEquals(FallbackFuelBurnMilestoneStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newFallbackFuelBurnMilestone("code-562", "title-382")
        try {
            item.moveTo(FallbackFuelBurnMilestoneStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackFuelBurnMilestoneStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newFallbackFuelBurnMilestone("code-562", "title-382")
        item.status = FallbackFuelBurnMilestoneStatus.ARCHIVED
        assertFails("move") { item.moveTo(FallbackFuelBurnMilestoneStatus.OPEN) }
    }
}
