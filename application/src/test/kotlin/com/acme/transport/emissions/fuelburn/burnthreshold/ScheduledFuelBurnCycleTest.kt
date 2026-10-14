package com.acme.transport.emissions.fuelburn.burnthreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledFuelBurnCycleTest {
    @Test
    fun `starts in open`() {
        val item = newScheduledFuelBurnCycle("code-101", "name-697")
        assertEquals(ScheduledFuelBurnCycleStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newScheduledFuelBurnCycle("code-101", "name-697")
        try {
            item.moveTo(ScheduledFuelBurnCycleStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledFuelBurnCycleStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newScheduledFuelBurnCycle("code-101", "name-697")
        item.status = ScheduledFuelBurnCycleStatus.ARCHIVED
        assertFails("move") { item.moveTo(ScheduledFuelBurnCycleStatus.OPEN) }
    }
}
