package com.acme.transport.emissions.fuelburn.burnrequest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedFuelBurnCycleTest {
    @Test
    fun `starts in idle`() {
        val item = newBatchedFuelBurnCycle("code-15", "title-889")
        assertEquals(BatchedFuelBurnCycleStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newBatchedFuelBurnCycle("code-15", "title-889")
        try {
            item.moveTo(BatchedFuelBurnCycleStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedFuelBurnCycleStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newBatchedFuelBurnCycle("code-15", "title-889")
        item.status = BatchedFuelBurnCycleStatus.STOPPED
        assertFails("move") { item.moveTo(BatchedFuelBurnCycleStatus.IDLE) }
    }
}
