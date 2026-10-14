package com.acme.transport.crewing.roster.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RosterNoticeTest {
    @Test
    fun `starts in created`() {
        val item = newRosterNotice("code-219", "title-556")
        assertEquals(RosterNoticeStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newRosterNotice("code-219", "title-556")
        try {
            item.moveTo(RosterNoticeStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RosterNoticeStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRosterNotice("code-219", "title-556")
        item.status = RosterNoticeStatus.RETIRED
        assertFails("move") { item.moveTo(RosterNoticeStatus.CREATED) }
    }
}
