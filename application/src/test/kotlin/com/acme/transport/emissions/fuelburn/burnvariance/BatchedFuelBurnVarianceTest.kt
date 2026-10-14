package com.acme.transport.emissions.fuelburn.burnvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedFuelBurnVarianceTest {
    @Test
    fun `starts in idle`() {
        val item = newBatchedFuelBurnVariance("code-81", "label-105")
        assertEquals(BatchedFuelBurnVarianceStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newBatchedFuelBurnVariance("code-81", "label-105")
        try {
            item.moveTo(BatchedFuelBurnVarianceStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedFuelBurnVarianceStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newBatchedFuelBurnVariance("code-81", "label-105")
        item.status = BatchedFuelBurnVarianceStatus.STOPPED
        assertFails("move") { item.moveTo(BatchedFuelBurnVarianceStatus.IDLE) }
    }
}
