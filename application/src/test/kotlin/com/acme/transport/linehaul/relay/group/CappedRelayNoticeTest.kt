package com.acme.transport.linehaul.relay.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedRelayNoticeTest {
    @Test
    fun `starts in draft`() {
        val item = newCappedRelayNotice("code-835", "title-912")
        assertEquals(CappedRelayNoticeStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newCappedRelayNotice("code-835", "title-912")
        try {
            item.moveTo(CappedRelayNoticeStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedRelayNoticeStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newCappedRelayNotice("code-835", "title-912")
        item.status = CappedRelayNoticeStatus.CLOSED
        assertFails("move") { item.moveTo(CappedRelayNoticeStatus.DRAFT) }
    }
}
