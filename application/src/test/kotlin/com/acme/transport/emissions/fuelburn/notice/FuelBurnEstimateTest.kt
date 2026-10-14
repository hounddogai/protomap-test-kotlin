package com.acme.transport.emissions.fuelburn.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelBurnEstimateTest {
    @Test
    fun `starts in proposed`() {
        val item = newFuelBurnEstimate("code-866", "title-228")
        assertEquals(FuelBurnEstimateStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newFuelBurnEstimate("code-866", "title-228")
        try {
            item.moveTo(FuelBurnEstimateStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelBurnEstimateStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newFuelBurnEstimate("code-866", "title-228")
        item.status = FuelBurnEstimateStatus.WITHDRAWN
        assertFails("move") { item.moveTo(FuelBurnEstimateStatus.PROPOSED) }
    }
}
