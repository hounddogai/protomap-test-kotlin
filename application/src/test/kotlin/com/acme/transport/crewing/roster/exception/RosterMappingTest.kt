package com.acme.transport.crewing.roster.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RosterMappingTest {
    @Test
    fun `starts in requested`() {
        val item = newRosterMapping("code-166", "label-664")
        assertEquals(RosterMappingStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newRosterMapping("code-166", "label-664")
        try {
            item.moveTo(RosterMappingStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RosterMappingStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRosterMapping("code-166", "label-664")
        item.status = RosterMappingStatus.EXPIRED
        assertFails("move") { item.moveTo(RosterMappingStatus.REQUESTED) }
    }
}
