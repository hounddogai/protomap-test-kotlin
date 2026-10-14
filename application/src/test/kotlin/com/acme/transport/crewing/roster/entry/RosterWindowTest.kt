package com.acme.transport.crewing.roster.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RosterWindowTest {
    @Test
    fun `starts in requested`() {
        val item = newRosterWindow("code-524", "name-45")
        assertEquals(RosterWindowStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newRosterWindow("code-524", "name-45")
        try {
            item.moveTo(RosterWindowStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RosterWindowStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRosterWindow("code-524", "name-45")
        item.status = RosterWindowStatus.EXPIRED
        assertFails("move") { item.moveTo(RosterWindowStatus.REQUESTED) }
    }
}
