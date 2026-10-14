package com.acme.transport.crewing.roster.rosteragreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedRosterNoticeTest {
    @Test
    fun `starts in proposed`() {
        val item = newCappedRosterNotice("code-285", "label-564")
        assertEquals(CappedRosterNoticeStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newCappedRosterNotice("code-285", "label-564")
        try {
            item.moveTo(CappedRosterNoticeStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedRosterNoticeStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newCappedRosterNotice("code-285", "label-564")
        item.status = CappedRosterNoticeStatus.WITHDRAWN
        assertFails("move") { item.moveTo(CappedRosterNoticeStatus.PROPOSED) }
    }
}
