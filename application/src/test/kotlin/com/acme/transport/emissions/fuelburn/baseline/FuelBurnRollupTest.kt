package com.acme.transport.emissions.fuelburn.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelBurnRollupTest {
    @Test
    fun `starts in planned`() {
        val item = newFuelBurnRollup("code-349", "name-500")
        assertEquals(FuelBurnRollupStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newFuelBurnRollup("code-349", "name-500")
        try {
            item.moveTo(FuelBurnRollupStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelBurnRollupStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newFuelBurnRollup("code-349", "name-500")
        item.status = FuelBurnRollupStatus.CANCELLED
        assertFails("move") { item.moveTo(FuelBurnRollupStatus.PLANNED) }
    }
}
