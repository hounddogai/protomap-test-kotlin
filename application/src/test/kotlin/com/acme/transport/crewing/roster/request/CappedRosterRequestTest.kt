package com.acme.transport.crewing.roster.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedRosterRequestTest {
    @Test
    fun `starts in draft`() {
        val item = newCappedRosterRequest("code-79", "name-238")
        assertEquals(CappedRosterRequestStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newCappedRosterRequest("code-79", "name-238")
        try {
            item.moveTo(CappedRosterRequestStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedRosterRequestStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newCappedRosterRequest("code-79", "name-238")
        item.status = CappedRosterRequestStatus.CLOSED
        assertFails("move") { item.moveTo(CappedRosterRequestStatus.DRAFT) }
    }
}
