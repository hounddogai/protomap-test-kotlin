package com.acme.transport.linehaul.trailer.trailerbatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedTrailerProfileTest {
    @Test
    fun `starts in requested`() {
        val item = newBatchedTrailerProfile("code-516", "label-657")
        assertEquals(BatchedTrailerProfileStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newBatchedTrailerProfile("code-516", "label-657")
        try {
            item.moveTo(BatchedTrailerProfileStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedTrailerProfileStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newBatchedTrailerProfile("code-516", "label-657")
        item.status = BatchedTrailerProfileStatus.EXPIRED
        assertFails("move") { item.moveTo(BatchedTrailerProfileStatus.REQUESTED) }
    }
}
