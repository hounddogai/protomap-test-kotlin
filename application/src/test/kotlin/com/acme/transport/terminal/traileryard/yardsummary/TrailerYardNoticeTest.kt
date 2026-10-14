package com.acme.transport.terminal.traileryard.yardsummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TrailerYardNoticeTest {
    @Test
    fun `starts in created`() {
        val item = newTrailerYardNotice("code-499", "title-193")
        assertEquals(TrailerYardNoticeStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newTrailerYardNotice("code-499", "title-193")
        try {
            item.moveTo(TrailerYardNoticeStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TrailerYardNoticeStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newTrailerYardNotice("code-499", "title-193")
        item.status = TrailerYardNoticeStatus.RETIRED
        assertFails("move") { item.moveTo(TrailerYardNoticeStatus.CREATED) }
    }
}
