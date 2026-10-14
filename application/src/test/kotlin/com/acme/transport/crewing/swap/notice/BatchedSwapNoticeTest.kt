package com.acme.transport.crewing.swap.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedSwapNoticeTest {
    @Test
    fun `starts in proposed`() {
        val item = newBatchedSwapNotice("code-297", "title-216")
        assertEquals(BatchedSwapNoticeStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newBatchedSwapNotice("code-297", "title-216")
        try {
            item.moveTo(BatchedSwapNoticeStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedSwapNoticeStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newBatchedSwapNotice("code-297", "title-216")
        item.status = BatchedSwapNoticeStatus.WITHDRAWN
        assertFails("move") { item.moveTo(BatchedSwapNoticeStatus.PROPOSED) }
    }
}
