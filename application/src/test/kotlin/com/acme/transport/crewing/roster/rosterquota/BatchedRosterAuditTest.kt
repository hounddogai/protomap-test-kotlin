package com.acme.transport.crewing.roster.rosterquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedRosterAuditTest {
    @Test
    fun `starts in new`() {
        val item = newBatchedRosterAudit("code-245", "label-639")
        assertEquals(BatchedRosterAuditStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newBatchedRosterAudit("code-245", "label-639")
        try {
            item.moveTo(BatchedRosterAuditStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedRosterAuditStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newBatchedRosterAudit("code-245", "label-639")
        item.status = BatchedRosterAuditStatus.FAILED
        assertFails("move") { item.moveTo(BatchedRosterAuditStatus.NEW) }
    }
}
