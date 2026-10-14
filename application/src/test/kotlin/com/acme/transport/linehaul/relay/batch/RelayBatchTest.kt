package com.acme.transport.linehaul.relay.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RelayBatchTest {
    @Test
    fun `starts in new`() {
        val item = newRelayBatch("code-676", "label-620")
        assertEquals(RelayBatchStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newRelayBatch("code-676", "label-620")
        try {
            item.moveTo(RelayBatchStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RelayBatchStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRelayBatch("code-676", "label-620")
        item.status = RelayBatchStatus.FAILED
        assertFails("move") { item.moveTo(RelayBatchStatus.NEW) }
    }
}
