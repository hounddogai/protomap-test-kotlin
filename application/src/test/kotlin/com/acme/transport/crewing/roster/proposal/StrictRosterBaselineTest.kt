package com.acme.transport.crewing.roster.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictRosterBaselineTest {
    @Test
    fun `starts in requested`() {
        val item = newStrictRosterBaseline("code-707", "title-19")
        assertEquals(StrictRosterBaselineStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newStrictRosterBaseline("code-707", "title-19")
        try {
            item.moveTo(StrictRosterBaselineStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictRosterBaselineStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newStrictRosterBaseline("code-707", "title-19")
        item.status = StrictRosterBaselineStatus.EXPIRED
        assertFails("move") { item.moveTo(StrictRosterBaselineStatus.REQUESTED) }
    }
}
