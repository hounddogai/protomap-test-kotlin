package com.acme.transport.crewing.roster.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RosterSlotTest {
    @Test
    fun `starts in created`() {
        val item = newRosterSlot("code-387", "label-382")
        assertEquals(RosterSlotStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newRosterSlot("code-387", "label-382")
        try {
            item.moveTo(RosterSlotStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RosterSlotStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRosterSlot("code-387", "label-382")
        item.status = RosterSlotStatus.RETIRED
        assertFails("move") { item.moveTo(RosterSlotStatus.CREATED) }
    }
}
