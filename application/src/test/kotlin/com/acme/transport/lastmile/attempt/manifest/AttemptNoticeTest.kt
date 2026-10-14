package com.acme.transport.lastmile.attempt.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AttemptNoticeTest {
    @Test
    fun `starts in draft`() {
        val item = newAttemptNotice("code-387", "label-891")
        assertEquals(AttemptNoticeStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newAttemptNotice("code-387", "label-891")
        try {
            item.moveTo(AttemptNoticeStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AttemptNoticeStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newAttemptNotice("code-387", "label-891")
        item.status = AttemptNoticeStatus.CLOSED
        assertFails("move") { item.moveTo(AttemptNoticeStatus.DRAFT) }
    }
}
