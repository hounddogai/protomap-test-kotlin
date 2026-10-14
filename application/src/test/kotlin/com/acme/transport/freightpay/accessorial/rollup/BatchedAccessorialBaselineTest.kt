package com.acme.transport.freightpay.accessorial.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedAccessorialBaselineTest {
    @Test
    fun `starts in planned`() {
        val item = newBatchedAccessorialBaseline("code-345", "label-683")
        assertEquals(BatchedAccessorialBaselineStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newBatchedAccessorialBaseline("code-345", "label-683")
        try {
            item.moveTo(BatchedAccessorialBaselineStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedAccessorialBaselineStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newBatchedAccessorialBaseline("code-345", "label-683")
        item.status = BatchedAccessorialBaselineStatus.CANCELLED
        assertFails("move") { item.moveTo(BatchedAccessorialBaselineStatus.PLANNED) }
    }
}
