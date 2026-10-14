package com.acme.transport.customs.hold.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledHoldNoticeTest {
    @Test
    fun `starts in planned`() {
        val item = newScheduledHoldNotice("code-231", "name-72")
        assertEquals(ScheduledHoldNoticeStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newScheduledHoldNotice("code-231", "name-72")
        try {
            item.moveTo(ScheduledHoldNoticeStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledHoldNoticeStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newScheduledHoldNotice("code-231", "name-72")
        item.status = ScheduledHoldNoticeStatus.CANCELLED
        assertFails("move") { item.moveTo(ScheduledHoldNoticeStatus.PLANNED) }
    }
}
