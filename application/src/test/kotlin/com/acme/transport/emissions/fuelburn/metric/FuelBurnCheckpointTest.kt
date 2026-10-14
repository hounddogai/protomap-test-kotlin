package com.acme.transport.emissions.fuelburn.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelBurnCheckpointTest {
    @Test
    fun `starts in created`() {
        val item = newFuelBurnCheckpoint("code-287", "name-705")
        assertEquals(FuelBurnCheckpointStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newFuelBurnCheckpoint("code-287", "name-705")
        try {
            item.moveTo(FuelBurnCheckpointStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelBurnCheckpointStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newFuelBurnCheckpoint("code-287", "name-705")
        item.status = FuelBurnCheckpointStatus.RETIRED
        assertFails("move") { item.moveTo(FuelBurnCheckpointStatus.CREATED) }
    }
}
