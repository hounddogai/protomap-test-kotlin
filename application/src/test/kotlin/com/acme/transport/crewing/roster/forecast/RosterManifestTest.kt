package com.acme.transport.crewing.roster.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RosterManifestTest {
    @Test
    fun `starts in requested`() {
        val item = newRosterManifest("code-235", "title-754")
        assertEquals(RosterManifestStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newRosterManifest("code-235", "title-754")
        try {
            item.moveTo(RosterManifestStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RosterManifestStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRosterManifest("code-235", "title-754")
        item.status = RosterManifestStatus.EXPIRED
        assertFails("move") { item.moveTo(RosterManifestStatus.REQUESTED) }
    }
}
