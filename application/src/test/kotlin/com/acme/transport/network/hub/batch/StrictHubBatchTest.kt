package com.acme.transport.network.hub.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictHubBatchTest {
    @Test
    fun `starts in open`() {
        val item = newStrictHubBatch("code-999", "name-864")
        assertEquals(StrictHubBatchStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newStrictHubBatch("code-999", "name-864")
        try {
            item.moveTo(StrictHubBatchStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictHubBatchStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newStrictHubBatch("code-999", "name-864")
        item.status = StrictHubBatchStatus.ARCHIVED
        assertFails("move") { item.moveTo(StrictHubBatchStatus.OPEN) }
    }
}
