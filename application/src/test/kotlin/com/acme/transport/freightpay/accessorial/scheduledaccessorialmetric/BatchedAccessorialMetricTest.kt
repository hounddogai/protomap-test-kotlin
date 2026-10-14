package com.acme.transport.freightpay.accessorial.scheduledaccessorialmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedAccessorialMetricTest {
    @Test
    fun `starts in idle`() {
        val item = newBatchedAccessorialMetric("code-536", "label-744")
        assertEquals(BatchedAccessorialMetricStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newBatchedAccessorialMetric("code-536", "label-744")
        try {
            item.moveTo(BatchedAccessorialMetricStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedAccessorialMetricStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newBatchedAccessorialMetric("code-536", "label-744")
        item.status = BatchedAccessorialMetricStatus.STOPPED
        assertFails("move") { item.moveTo(BatchedAccessorialMetricStatus.IDLE) }
    }
}
