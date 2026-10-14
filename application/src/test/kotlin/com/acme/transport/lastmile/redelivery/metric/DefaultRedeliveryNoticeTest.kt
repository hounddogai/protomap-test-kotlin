package com.acme.transport.lastmile.redelivery.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultRedeliveryNoticeTest {
    @Test
    fun `starts in idle`() {
        val item = newDefaultRedeliveryNotice("code-902", "name-227")
        assertEquals(DefaultRedeliveryNoticeStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newDefaultRedeliveryNotice("code-902", "name-227")
        try {
            item.moveTo(DefaultRedeliveryNoticeStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultRedeliveryNoticeStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newDefaultRedeliveryNotice("code-902", "name-227")
        item.status = DefaultRedeliveryNoticeStatus.STOPPED
        assertFails("move") { item.moveTo(DefaultRedeliveryNoticeStatus.IDLE) }
    }
}
