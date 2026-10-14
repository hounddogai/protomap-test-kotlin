package com.acme.transport.safety.nearmiss.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class NearMissNoticeTest {
    @Test
    fun `starts in proposed`() {
        val item = newNearMissNotice("code-962", "title-789")
        assertEquals(NearMissNoticeStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newNearMissNotice("code-962", "title-789")
        try {
            item.moveTo(NearMissNoticeStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(NearMissNoticeStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newNearMissNotice("code-962", "title-789")
        item.status = NearMissNoticeStatus.WITHDRAWN
        assertFails("move") { item.moveTo(NearMissNoticeStatus.PROPOSED) }
    }
}
