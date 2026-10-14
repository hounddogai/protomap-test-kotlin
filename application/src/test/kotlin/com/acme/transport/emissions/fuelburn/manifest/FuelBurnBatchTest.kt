package com.acme.transport.emissions.fuelburn.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelBurnBatchTest {
    @Test
    fun `starts in planned`() {
        val item = newFuelBurnBatch("code-53", "name-531")
        assertEquals(FuelBurnBatchStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newFuelBurnBatch("code-53", "name-531")
        try {
            item.moveTo(FuelBurnBatchStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelBurnBatchStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newFuelBurnBatch("code-53", "name-531")
        item.status = FuelBurnBatchStatus.CANCELLED
        assertFails("move") { item.moveTo(FuelBurnBatchStatus.PLANNED) }
    }
}
