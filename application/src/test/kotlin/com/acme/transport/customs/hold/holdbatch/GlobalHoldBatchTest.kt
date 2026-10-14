package com.acme.transport.customs.hold.holdbatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalHoldBatchTest {
    @Test
    fun `starts in new`() {
        val item = newGlobalHoldBatch("code-688", "label-960")
        assertEquals(GlobalHoldBatchStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newGlobalHoldBatch("code-688", "label-960")
        try {
            item.moveTo(GlobalHoldBatchStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalHoldBatchStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newGlobalHoldBatch("code-688", "label-960")
        item.status = GlobalHoldBatchStatus.FAILED
        assertFails("move") { item.moveTo(GlobalHoldBatchStatus.NEW) }
    }
}
