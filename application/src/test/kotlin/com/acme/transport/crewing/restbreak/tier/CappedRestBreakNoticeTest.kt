package com.acme.transport.crewing.restbreak.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedRestBreakNoticeTest {
    @Test
    fun `starts in requested`() {
        val item = newCappedRestBreakNotice("code-732", "name-139")
        assertEquals(CappedRestBreakNoticeStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newCappedRestBreakNotice("code-732", "name-139")
        try {
            item.moveTo(CappedRestBreakNoticeStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedRestBreakNoticeStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newCappedRestBreakNotice("code-732", "name-139")
        item.status = CappedRestBreakNoticeStatus.EXPIRED
        assertFails("move") { item.moveTo(CappedRestBreakNoticeStatus.REQUESTED) }
    }
}
