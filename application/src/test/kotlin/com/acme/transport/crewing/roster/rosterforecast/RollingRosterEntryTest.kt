package com.acme.transport.crewing.roster.rosterforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingRosterEntryTest {
    @Test
    fun `starts in requested`() {
        val item = newRollingRosterEntry("code-17", "label-175")
        assertEquals(RollingRosterEntryStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newRollingRosterEntry("code-17", "label-175")
        try {
            item.moveTo(RollingRosterEntryStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingRosterEntryStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRollingRosterEntry("code-17", "label-175")
        item.status = RollingRosterEntryStatus.EXPIRED
        assertFails("move") { item.moveTo(RollingRosterEntryStatus.REQUESTED) }
    }
}
