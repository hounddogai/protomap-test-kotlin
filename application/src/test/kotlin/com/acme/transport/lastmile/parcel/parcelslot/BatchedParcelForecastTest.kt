package com.acme.transport.lastmile.parcel.parcelslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedParcelForecastTest {
    @Test
    fun `starts in planned`() {
        val item = newBatchedParcelForecast("code-107", "title-999")
        assertEquals(BatchedParcelForecastStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newBatchedParcelForecast("code-107", "title-999")
        try {
            item.moveTo(BatchedParcelForecastStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedParcelForecastStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newBatchedParcelForecast("code-107", "title-999")
        item.status = BatchedParcelForecastStatus.CANCELLED
        assertFails("move") { item.moveTo(BatchedParcelForecastStatus.PLANNED) }
    }
}
