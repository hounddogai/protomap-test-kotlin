package com.acme.transport.linehaul.tender.tenderforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedTenderForecastTest {
    @Test
    fun `starts in proposed`() {
        val item = newBatchedTenderForecast("code-304", "label-138")
        assertEquals(BatchedTenderForecastStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newBatchedTenderForecast("code-304", "label-138")
        try {
            item.moveTo(BatchedTenderForecastStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedTenderForecastStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newBatchedTenderForecast("code-304", "label-138")
        item.status = BatchedTenderForecastStatus.WITHDRAWN
        assertFails("move") { item.moveTo(BatchedTenderForecastStatus.PROPOSED) }
    }
}
