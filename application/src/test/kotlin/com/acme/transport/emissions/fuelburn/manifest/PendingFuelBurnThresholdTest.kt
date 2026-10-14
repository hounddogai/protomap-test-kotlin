package com.acme.transport.emissions.fuelburn.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingFuelBurnThresholdTest {
    @Test
    fun `starts in new`() {
        val item = newPendingFuelBurnThreshold("code-500", "label-421")
        assertEquals(PendingFuelBurnThresholdStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newPendingFuelBurnThreshold("code-500", "label-421")
        try {
            item.moveTo(PendingFuelBurnThresholdStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingFuelBurnThresholdStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newPendingFuelBurnThreshold("code-500", "label-421")
        item.status = PendingFuelBurnThresholdStatus.FAILED
        assertFails("move") { item.moveTo(PendingFuelBurnThresholdStatus.NEW) }
    }
}
