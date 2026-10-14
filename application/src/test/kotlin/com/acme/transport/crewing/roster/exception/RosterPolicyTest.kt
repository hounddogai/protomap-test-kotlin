package com.acme.transport.crewing.roster.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RosterPolicyTest {
    @Test
    fun `starts in new`() {
        val item = newRosterPolicy("code-880", "label-28")
        assertEquals(RosterPolicyStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newRosterPolicy("code-880", "label-28")
        try {
            item.moveTo(RosterPolicyStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RosterPolicyStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRosterPolicy("code-880", "label-28")
        item.status = RosterPolicyStatus.FAILED
        assertFails("move") { item.moveTo(RosterPolicyStatus.NEW) }
    }
}
