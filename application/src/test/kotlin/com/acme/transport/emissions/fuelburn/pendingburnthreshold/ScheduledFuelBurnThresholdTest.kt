package com.acme.transport.emissions.fuelburn.pendingburnthreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledFuelBurnThresholdTest {
    @Test
    fun `starts in requested`() {
        val item = newScheduledFuelBurnThreshold("code-678", "name-356")
        assertEquals(ScheduledFuelBurnThresholdStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newScheduledFuelBurnThreshold("code-678", "name-356")
        try {
            item.moveTo(ScheduledFuelBurnThresholdStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledFuelBurnThresholdStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newScheduledFuelBurnThreshold("code-678", "name-356")
        item.status = ScheduledFuelBurnThresholdStatus.EXPIRED
        assertFails("move") { item.moveTo(ScheduledFuelBurnThresholdStatus.REQUESTED) }
    }
}
