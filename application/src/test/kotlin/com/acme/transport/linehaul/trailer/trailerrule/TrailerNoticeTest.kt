package com.acme.transport.linehaul.trailer.trailerrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TrailerNoticeTest {
    @Test
    fun `starts in draft`() {
        val item = newTrailerNotice("code-756", "name-317")
        assertEquals(TrailerNoticeStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newTrailerNotice("code-756", "name-317")
        try {
            item.moveTo(TrailerNoticeStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TrailerNoticeStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newTrailerNotice("code-756", "name-317")
        item.status = TrailerNoticeStatus.CLOSED
        assertFails("move") { item.moveTo(TrailerNoticeStatus.DRAFT) }
    }
}
