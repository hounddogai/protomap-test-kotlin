package com.acme.transport.customs.clearance.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledClearanceNoticeTest {
    @Test
    fun `starts in new`() {
        val item = newScheduledClearanceNotice("code-660", "name-490")
        assertEquals(ScheduledClearanceNoticeStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newScheduledClearanceNotice("code-660", "name-490")
        try {
            item.moveTo(ScheduledClearanceNoticeStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledClearanceNoticeStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newScheduledClearanceNotice("code-660", "name-490")
        item.status = ScheduledClearanceNoticeStatus.FAILED
        assertFails("move") { item.moveTo(ScheduledClearanceNoticeStatus.NEW) }
    }
}
