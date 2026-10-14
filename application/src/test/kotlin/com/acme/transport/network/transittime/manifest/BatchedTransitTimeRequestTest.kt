package com.acme.transport.network.transittime.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedTransitTimeRequestTest {
    @Test
    fun `starts in created`() {
        val item = newBatchedTransitTimeRequest("code-571", "label-926")
        assertEquals(BatchedTransitTimeRequestStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newBatchedTransitTimeRequest("code-571", "label-926")
        try {
            item.moveTo(BatchedTransitTimeRequestStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedTransitTimeRequestStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newBatchedTransitTimeRequest("code-571", "label-926")
        item.status = BatchedTransitTimeRequestStatus.RETIRED
        assertFails("move") { item.moveTo(BatchedTransitTimeRequestStatus.CREATED) }
    }
}
