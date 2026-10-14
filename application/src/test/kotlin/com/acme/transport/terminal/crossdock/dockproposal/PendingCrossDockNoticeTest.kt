package com.acme.transport.terminal.crossdock.dockproposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingCrossDockNoticeTest {
    @Test
    fun `starts in open`() {
        val item = newPendingCrossDockNotice("code-503", "label-212")
        assertEquals(PendingCrossDockNoticeStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newPendingCrossDockNotice("code-503", "label-212")
        try {
            item.moveTo(PendingCrossDockNoticeStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingCrossDockNoticeStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newPendingCrossDockNotice("code-503", "label-212")
        item.status = PendingCrossDockNoticeStatus.ARCHIVED
        assertFails("move") { item.moveTo(PendingCrossDockNoticeStatus.OPEN) }
    }
}
