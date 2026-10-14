package com.acme.transport.customs.hold.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HoldBatchTest {
    @Test
    fun `starts in requested`() {
        val item = newHoldBatch("code-102", "title-672")
        assertEquals(HoldBatchStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newHoldBatch("code-102", "title-672")
        try {
            item.moveTo(HoldBatchStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HoldBatchStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newHoldBatch("code-102", "title-672")
        item.status = HoldBatchStatus.EXPIRED
        assertFails("move") { item.moveTo(HoldBatchStatus.REQUESTED) }
    }
}
