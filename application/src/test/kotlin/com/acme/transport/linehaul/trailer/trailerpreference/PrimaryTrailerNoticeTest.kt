package com.acme.transport.linehaul.trailer.trailerpreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryTrailerNoticeTest {
    @Test
    fun `starts in planned`() {
        val item = newPrimaryTrailerNotice("code-391", "title-320")
        assertEquals(PrimaryTrailerNoticeStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newPrimaryTrailerNotice("code-391", "title-320")
        try {
            item.moveTo(PrimaryTrailerNoticeStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryTrailerNoticeStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newPrimaryTrailerNotice("code-391", "title-320")
        item.status = PrimaryTrailerNoticeStatus.CANCELLED
        assertFails("move") { item.moveTo(PrimaryTrailerNoticeStatus.PLANNED) }
    }
}
