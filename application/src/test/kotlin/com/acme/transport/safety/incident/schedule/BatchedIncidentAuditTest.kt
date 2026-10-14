package com.acme.transport.safety.incident.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedIncidentAuditTest {
    @Test
    fun `starts in new`() {
        val item = newBatchedIncidentAudit("code-262", "title-505")
        assertEquals(BatchedIncidentAuditStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newBatchedIncidentAudit("code-262", "title-505")
        try {
            item.moveTo(BatchedIncidentAuditStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedIncidentAuditStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newBatchedIncidentAudit("code-262", "title-505")
        item.status = BatchedIncidentAuditStatus.FAILED
        assertFails("move") { item.moveTo(BatchedIncidentAuditStatus.NEW) }
    }
}
