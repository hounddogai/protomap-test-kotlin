package com.acme.transport.crewing.roster.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RosterProfileTest {
    @Test
    fun `starts in created`() {
        val item = newRosterProfile("code-400", "label-49")
        assertEquals(RosterProfileStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newRosterProfile("code-400", "label-49")
        try {
            item.moveTo(RosterProfileStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RosterProfileStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRosterProfile("code-400", "label-49")
        item.status = RosterProfileStatus.RETIRED
        assertFails("move") { item.moveTo(RosterProfileStatus.CREATED) }
    }
}
