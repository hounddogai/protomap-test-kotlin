package com.acme.transport.freightpay.audit.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AuditCheckpointTest {
    @Test
    fun `starts in idle`() {
        val item = newAuditCheckpoint("code-462", "name-444")
        assertEquals(AuditCheckpointStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newAuditCheckpoint("code-462", "name-444")
        try {
            item.moveTo(AuditCheckpointStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AuditCheckpointStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newAuditCheckpoint("code-462", "name-444")
        item.status = AuditCheckpointStatus.STOPPED
        assertFails("move") { item.moveTo(AuditCheckpointStatus.IDLE) }
    }
}
