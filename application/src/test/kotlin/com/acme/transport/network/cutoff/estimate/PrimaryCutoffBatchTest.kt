package com.acme.transport.network.cutoff.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryCutoffBatchTest {
    @Test
    fun `starts in draft`() {
        val item = newPrimaryCutoffBatch("code-90", "name-376")
        assertEquals(PrimaryCutoffBatchStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newPrimaryCutoffBatch("code-90", "name-376")
        try {
            item.moveTo(PrimaryCutoffBatchStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryCutoffBatchStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newPrimaryCutoffBatch("code-90", "name-376")
        item.status = PrimaryCutoffBatchStatus.CLOSED
        assertFails("move") { item.moveTo(PrimaryCutoffBatchStatus.DRAFT) }
    }
}
