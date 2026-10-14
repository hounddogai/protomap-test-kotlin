package com.acme.transport.lastmile.redelivery.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RedeliveryNoticeTest {
    @Test
    fun `starts in new`() {
        val item = newRedeliveryNotice("code-16", "label-711")
        assertEquals(RedeliveryNoticeStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newRedeliveryNotice("code-16", "label-711")
        try {
            item.moveTo(RedeliveryNoticeStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RedeliveryNoticeStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRedeliveryNotice("code-16", "label-711")
        item.status = RedeliveryNoticeStatus.FAILED
        assertFails("move") { item.moveTo(RedeliveryNoticeStatus.NEW) }
    }
}
