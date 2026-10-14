package com.acme.transport.crewing.roster.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RosterDigestTest {
    @Test
    fun `starts in created`() {
        val item = newRosterDigest("code-333", "name-649")
        assertEquals(RosterDigestStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newRosterDigest("code-333", "name-649")
        try {
            item.moveTo(RosterDigestStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RosterDigestStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRosterDigest("code-333", "name-649")
        item.status = RosterDigestStatus.RETIRED
        assertFails("move") { item.moveTo(RosterDigestStatus.CREATED) }
    }
}
