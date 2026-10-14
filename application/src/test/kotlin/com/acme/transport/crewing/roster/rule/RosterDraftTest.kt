package com.acme.transport.crewing.roster.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RosterDraftTest {
    @Test
    fun `starts in new`() {
        val item = newRosterDraft("code-993", "label-874")
        assertEquals(RosterDraftStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newRosterDraft("code-993", "label-874")
        try {
            item.moveTo(RosterDraftStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RosterDraftStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRosterDraft("code-993", "label-874")
        item.status = RosterDraftStatus.FAILED
        assertFails("move") { item.moveTo(RosterDraftStatus.NEW) }
    }
}
