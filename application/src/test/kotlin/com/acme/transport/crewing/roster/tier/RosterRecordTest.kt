package com.acme.transport.crewing.roster.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RosterRecordTest {
    @Test
    fun `starts in idle`() {
        val item = newRosterRecord("code-948", "title-842")
        assertEquals(RosterRecordStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newRosterRecord("code-948", "title-842")
        try {
            item.moveTo(RosterRecordStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RosterRecordStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRosterRecord("code-948", "title-842")
        item.status = RosterRecordStatus.STOPPED
        assertFails("move") { item.moveTo(RosterRecordStatus.IDLE) }
    }
}
