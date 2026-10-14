package com.acme.transport.crewing.roster.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingRosterRollupTest {
    @Test
    fun `starts in new`() {
        val item = newPendingRosterRollup("code-233", "title-277")
        assertEquals(PendingRosterRollupStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newPendingRosterRollup("code-233", "title-277")
        try {
            item.moveTo(PendingRosterRollupStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingRosterRollupStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newPendingRosterRollup("code-233", "title-277")
        item.status = PendingRosterRollupStatus.FAILED
        assertFails("move") { item.moveTo(PendingRosterRollupStatus.NEW) }
    }
}
