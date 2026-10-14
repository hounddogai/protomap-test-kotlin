package com.acme.transport.network.hub.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HubBatchTest {
    @Test
    fun `starts in draft`() {
        val item = newHubBatch("code-160", "name-559")
        assertEquals(HubBatchStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newHubBatch("code-160", "name-559")
        try {
            item.moveTo(HubBatchStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HubBatchStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newHubBatch("code-160", "name-559")
        item.status = HubBatchStatus.CLOSED
        assertFails("move") { item.moveTo(HubBatchStatus.DRAFT) }
    }
}
