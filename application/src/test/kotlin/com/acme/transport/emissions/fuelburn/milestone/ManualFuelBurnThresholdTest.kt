package com.acme.transport.emissions.fuelburn.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualFuelBurnThresholdTest {
    @Test
    fun `starts in idle`() {
        val item = newManualFuelBurnThreshold("code-650", "title-472")
        assertEquals(ManualFuelBurnThresholdStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newManualFuelBurnThreshold("code-650", "title-472")
        try {
            item.moveTo(ManualFuelBurnThresholdStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualFuelBurnThresholdStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newManualFuelBurnThreshold("code-650", "title-472")
        item.status = ManualFuelBurnThresholdStatus.STOPPED
        assertFails("move") { item.moveTo(ManualFuelBurnThresholdStatus.IDLE) }
    }
}
