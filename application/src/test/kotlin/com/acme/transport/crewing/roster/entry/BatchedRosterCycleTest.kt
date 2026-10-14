package com.acme.transport.crewing.roster.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedRosterCycleTest {
    @Test
    fun `starts in proposed`() {
        val item = newBatchedRosterCycle("code-799", "label-572")
        assertEquals(BatchedRosterCycleStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newBatchedRosterCycle("code-799", "label-572")
        try {
            item.moveTo(BatchedRosterCycleStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedRosterCycleStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newBatchedRosterCycle("code-799", "label-572")
        item.status = BatchedRosterCycleStatus.WITHDRAWN
        assertFails("move") { item.moveTo(BatchedRosterCycleStatus.PROPOSED) }
    }
}
