package com.acme.transport.network.transittime.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedTransitTimeNoticeTest {
    @Test
    fun `starts in new`() {
        val item = newCappedTransitTimeNotice("code-456", "title-263")
        assertEquals(CappedTransitTimeNoticeStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newCappedTransitTimeNotice("code-456", "title-263")
        try {
            item.moveTo(CappedTransitTimeNoticeStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedTransitTimeNoticeStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newCappedTransitTimeNotice("code-456", "title-263")
        item.status = CappedTransitTimeNoticeStatus.FAILED
        assertFails("move") { item.moveTo(CappedTransitTimeNoticeStatus.NEW) }
    }
}
