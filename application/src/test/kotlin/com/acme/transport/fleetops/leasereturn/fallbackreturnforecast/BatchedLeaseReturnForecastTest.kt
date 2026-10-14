package com.acme.transport.fleetops.leasereturn.fallbackreturnforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedLeaseReturnForecastTest {
    @Test
    fun `starts in planned`() {
        val item = newBatchedLeaseReturnForecast("code-858", "title-465")
        assertEquals(BatchedLeaseReturnForecastStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newBatchedLeaseReturnForecast("code-858", "title-465")
        try {
            item.moveTo(BatchedLeaseReturnForecastStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedLeaseReturnForecastStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newBatchedLeaseReturnForecast("code-858", "title-465")
        item.status = BatchedLeaseReturnForecastStatus.CANCELLED
        assertFails("move") { item.moveTo(BatchedLeaseReturnForecastStatus.PLANNED) }
    }
}
