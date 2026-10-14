package com.acme.transport.network.transittime.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredTransitTimeNoticeTest {
    @Test
    fun `starts in draft`() {
        val item = newTieredTransitTimeNotice("code-560", "title-790")
        assertEquals(TieredTransitTimeNoticeStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newTieredTransitTimeNotice("code-560", "title-790")
        try {
            item.moveTo(TieredTransitTimeNoticeStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredTransitTimeNoticeStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newTieredTransitTimeNotice("code-560", "title-790")
        item.status = TieredTransitTimeNoticeStatus.CLOSED
        assertFails("move") { item.moveTo(TieredTransitTimeNoticeStatus.DRAFT) }
    }
}
