package com.acme.transport.lastmile.redelivery.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledRedeliveryNoticeTest {
    @Test
    fun `starts in open`() {
        val item = newScheduledRedeliveryNotice("code-224", "title-900")
        assertEquals(ScheduledRedeliveryNoticeStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newScheduledRedeliveryNotice("code-224", "title-900")
        try {
            item.moveTo(ScheduledRedeliveryNoticeStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledRedeliveryNoticeStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newScheduledRedeliveryNotice("code-224", "title-900")
        item.status = ScheduledRedeliveryNoticeStatus.ARCHIVED
        assertFails("move") { item.moveTo(ScheduledRedeliveryNoticeStatus.OPEN) }
    }
}
