package com.acme.transport.freightpay.accessorial.accessorialadjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledAccessorialNoticeTest {
    @Test
    fun `starts in new`() {
        val item = newScheduledAccessorialNotice("code-155", "name-618")
        assertEquals(ScheduledAccessorialNoticeStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newScheduledAccessorialNotice("code-155", "name-618")
        try {
            item.moveTo(ScheduledAccessorialNoticeStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledAccessorialNoticeStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newScheduledAccessorialNotice("code-155", "name-618")
        item.status = ScheduledAccessorialNoticeStatus.FAILED
        assertFails("move") { item.moveTo(ScheduledAccessorialNoticeStatus.NEW) }
    }
}
