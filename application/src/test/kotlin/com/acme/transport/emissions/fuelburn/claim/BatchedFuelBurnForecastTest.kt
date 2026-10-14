package com.acme.transport.emissions.fuelburn.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedFuelBurnForecastTest {
    @Test
    fun `starts in open`() {
        val item = newBatchedFuelBurnForecast("code-952", "label-866")
        assertEquals(BatchedFuelBurnForecastStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newBatchedFuelBurnForecast("code-952", "label-866")
        try {
            item.moveTo(BatchedFuelBurnForecastStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedFuelBurnForecastStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newBatchedFuelBurnForecast("code-952", "label-866")
        item.status = BatchedFuelBurnForecastStatus.ARCHIVED
        assertFails("move") { item.moveTo(BatchedFuelBurnForecastStatus.OPEN) }
    }
}
