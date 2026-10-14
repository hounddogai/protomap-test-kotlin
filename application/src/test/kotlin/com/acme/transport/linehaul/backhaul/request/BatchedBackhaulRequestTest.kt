package com.acme.transport.linehaul.backhaul.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedBackhaulRequestTest {
    @Test
    fun `starts in requested`() {
        val item = newBatchedBackhaulRequest("code-595", "label-806")
        assertEquals(BatchedBackhaulRequestStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newBatchedBackhaulRequest("code-595", "label-806")
        try {
            item.moveTo(BatchedBackhaulRequestStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedBackhaulRequestStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newBatchedBackhaulRequest("code-595", "label-806")
        item.status = BatchedBackhaulRequestStatus.EXPIRED
        assertFails("move") { item.moveTo(BatchedBackhaulRequestStatus.REQUESTED) }
    }
}
