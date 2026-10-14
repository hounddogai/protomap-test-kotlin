package com.acme.transport.crewing.roster.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RosterRequestTest {
    @Test
    fun `starts in draft`() {
        val item = newRosterRequest("code-619", "label-235")
        assertEquals(RosterRequestStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newRosterRequest("code-619", "label-235")
        try {
            item.moveTo(RosterRequestStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RosterRequestStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newRosterRequest("code-619", "label-235")
        item.status = RosterRequestStatus.CLOSED
        assertFails("move") { item.moveTo(RosterRequestStatus.DRAFT) }
    }
}
