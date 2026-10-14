package com.acme.transport.linehaul.backhaul.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedBackhaulTargetTest {
    @Test
    fun `starts in new`() {
        val item = newBatchedBackhaulTarget("code-480", "label-855")
        assertEquals(BatchedBackhaulTargetStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newBatchedBackhaulTarget("code-480", "label-855")
        try {
            item.moveTo(BatchedBackhaulTargetStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedBackhaulTargetStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newBatchedBackhaulTarget("code-480", "label-855")
        item.status = BatchedBackhaulTargetStatus.FAILED
        assertFails("move") { item.moveTo(BatchedBackhaulTargetStatus.NEW) }
    }
}
