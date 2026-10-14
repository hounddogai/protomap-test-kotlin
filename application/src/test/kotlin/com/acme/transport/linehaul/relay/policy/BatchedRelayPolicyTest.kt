package com.acme.transport.linehaul.relay.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedRelayPolicyTest {
    @Test
    fun `starts in idle`() {
        val item = newBatchedRelayPolicy("code-274", "title-800")
        assertEquals(BatchedRelayPolicyStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newBatchedRelayPolicy("code-274", "title-800")
        try {
            item.moveTo(BatchedRelayPolicyStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedRelayPolicyStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newBatchedRelayPolicy("code-274", "title-800")
        item.status = BatchedRelayPolicyStatus.STOPPED
        assertFails("move") { item.moveTo(BatchedRelayPolicyStatus.IDLE) }
    }
}
