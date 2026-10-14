package com.acme.transport.crewing.roster.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictRosterRequestTest {
    @Test
    fun `starts in proposed`() {
        val item = newStrictRosterRequest("code-945", "label-452")
        assertEquals(StrictRosterRequestStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newStrictRosterRequest("code-945", "label-452")
        try {
            item.moveTo(StrictRosterRequestStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictRosterRequestStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newStrictRosterRequest("code-945", "label-452")
        item.status = StrictRosterRequestStatus.WITHDRAWN
        assertFails("move") { item.moveTo(StrictRosterRequestStatus.PROPOSED) }
    }
}
