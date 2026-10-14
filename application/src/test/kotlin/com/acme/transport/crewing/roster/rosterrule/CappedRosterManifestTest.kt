package com.acme.transport.crewing.roster.rosterrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedRosterManifestTest {
    @Test
    fun `starts in planned`() {
        val item = newCappedRosterManifest("code-754", "title-172")
        assertEquals(CappedRosterManifestStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newCappedRosterManifest("code-754", "title-172")
        try {
            item.moveTo(CappedRosterManifestStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedRosterManifestStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newCappedRosterManifest("code-754", "title-172")
        item.status = CappedRosterManifestStatus.CANCELLED
        assertFails("move") { item.moveTo(CappedRosterManifestStatus.PLANNED) }
    }
}
