package com.acme.transport.terminal.crossdock.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CrossDockNoticeTest {
    @Test
    fun `starts in open`() {
        val item = newCrossDockNotice("code-719", "name-514")
        assertEquals(CrossDockNoticeStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newCrossDockNotice("code-719", "name-514")
        try {
            item.moveTo(CrossDockNoticeStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CrossDockNoticeStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newCrossDockNotice("code-719", "name-514")
        item.status = CrossDockNoticeStatus.ARCHIVED
        assertFails("move") { item.moveTo(CrossDockNoticeStatus.OPEN) }
    }
}
