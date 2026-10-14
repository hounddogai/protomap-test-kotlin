package com.acme.transport.linehaul.relay.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RelayNoticeTest {
    @Test
    fun `starts in idle`() {
        val item = newRelayNotice("code-529", "label-662")
        assertEquals(RelayNoticeStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newRelayNotice("code-529", "label-662")
        try {
            item.moveTo(RelayNoticeStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RelayNoticeStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRelayNotice("code-529", "label-662")
        item.status = RelayNoticeStatus.STOPPED
        assertFails("move") { item.moveTo(RelayNoticeStatus.IDLE) }
    }
}
