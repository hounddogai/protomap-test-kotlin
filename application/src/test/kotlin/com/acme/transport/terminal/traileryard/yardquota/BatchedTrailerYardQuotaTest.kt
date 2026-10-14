package com.acme.transport.terminal.traileryard.yardquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedTrailerYardQuotaTest {
    @Test
    fun `starts in requested`() {
        val item = newBatchedTrailerYardQuota("code-215", "title-997")
        assertEquals(BatchedTrailerYardQuotaStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newBatchedTrailerYardQuota("code-215", "title-997")
        try {
            item.moveTo(BatchedTrailerYardQuotaStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedTrailerYardQuotaStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newBatchedTrailerYardQuota("code-215", "title-997")
        item.status = BatchedTrailerYardQuotaStatus.EXPIRED
        assertFails("move") { item.moveTo(BatchedTrailerYardQuotaStatus.REQUESTED) }
    }
}
