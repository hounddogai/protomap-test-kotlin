package com.acme.transport.safety.coaching.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CoachingNoticeTest {
    @Test
    fun `starts in open`() {
        val item = newCoachingNotice("code-319", "name-453")
        assertEquals(CoachingNoticeStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newCoachingNotice("code-319", "name-453")
        try {
            item.moveTo(CoachingNoticeStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CoachingNoticeStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newCoachingNotice("code-319", "name-453")
        item.status = CoachingNoticeStatus.ARCHIVED
        assertFails("move") { item.moveTo(CoachingNoticeStatus.OPEN) }
    }
}
