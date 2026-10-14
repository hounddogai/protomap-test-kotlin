package com.acme.transport.emissions.fuelburn.burnmilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalFuelBurnEstimateTest {
    @Test
    fun `starts in requested`() {
        val item = newGlobalFuelBurnEstimate("code-310", "label-945")
        assertEquals(GlobalFuelBurnEstimateStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newGlobalFuelBurnEstimate("code-310", "label-945")
        try {
            item.moveTo(GlobalFuelBurnEstimateStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalFuelBurnEstimateStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newGlobalFuelBurnEstimate("code-310", "label-945")
        item.status = GlobalFuelBurnEstimateStatus.EXPIRED
        assertFails("move") { item.moveTo(GlobalFuelBurnEstimateStatus.REQUESTED) }
    }
}
