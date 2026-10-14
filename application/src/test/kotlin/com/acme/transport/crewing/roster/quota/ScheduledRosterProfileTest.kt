package com.acme.transport.crewing.roster.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledRosterProfileTest {
    @Test
    fun `starts in new`() {
        val item = newScheduledRosterProfile("code-881", "label-971")
        assertEquals(ScheduledRosterProfileStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newScheduledRosterProfile("code-881", "label-971")
        try {
            item.moveTo(ScheduledRosterProfileStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledRosterProfileStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newScheduledRosterProfile("code-881", "label-971")
        item.status = ScheduledRosterProfileStatus.FAILED
        assertFails("move") { item.moveTo(ScheduledRosterProfileStatus.NEW) }
    }
}
