package com.acme.transport.crewing.roster.fallbackrosterforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RosterOverrideTest {
    @Test
    fun `starts in requested`() {
        val item = newRosterOverride("code-157", "label-756")
        assertEquals(RosterOverrideStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newRosterOverride("code-157", "label-756")
        try {
            item.moveTo(RosterOverrideStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RosterOverrideStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRosterOverride("code-157", "label-756")
        item.status = RosterOverrideStatus.EXPIRED
        assertFails("move") { item.moveTo(RosterOverrideStatus.REQUESTED) }
    }
}
