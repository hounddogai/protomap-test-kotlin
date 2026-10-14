package com.acme.transport.customs.hold.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HoldNoticeTest {
    @Test
    fun `starts in new`() {
        val item = newHoldNotice("code-126", "title-940")
        assertEquals(HoldNoticeStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newHoldNotice("code-126", "title-940")
        try {
            item.moveTo(HoldNoticeStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HoldNoticeStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newHoldNotice("code-126", "title-940")
        item.status = HoldNoticeStatus.FAILED
        assertFails("move") { item.moveTo(HoldNoticeStatus.NEW) }
    }
}
