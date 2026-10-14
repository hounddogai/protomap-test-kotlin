package com.acme.transport.lastmile.attempt.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultAttemptNoticeTest {
    @Test
    fun `starts in draft`() {
        val item = newDefaultAttemptNotice("code-156", "name-37")
        assertEquals(DefaultAttemptNoticeStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newDefaultAttemptNotice("code-156", "name-37")
        try {
            item.moveTo(DefaultAttemptNoticeStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultAttemptNoticeStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newDefaultAttemptNotice("code-156", "name-37")
        item.status = DefaultAttemptNoticeStatus.CLOSED
        assertFails("move") { item.moveTo(DefaultAttemptNoticeStatus.DRAFT) }
    }
}
