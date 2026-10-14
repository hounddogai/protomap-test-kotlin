package com.acme.transport.network.zone.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ZoneNoticeTest {
    @Test
    fun `starts in draft`() {
        val item = newZoneNotice("code-373", "title-920")
        assertEquals(ZoneNoticeStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newZoneNotice("code-373", "title-920")
        try {
            item.moveTo(ZoneNoticeStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ZoneNoticeStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newZoneNotice("code-373", "title-920")
        item.status = ZoneNoticeStatus.CLOSED
        assertFails("move") { item.moveTo(ZoneNoticeStatus.DRAFT) }
    }
}
