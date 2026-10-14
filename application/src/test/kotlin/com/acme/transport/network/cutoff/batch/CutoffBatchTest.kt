package com.acme.transport.network.cutoff.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CutoffBatchTest {
    @Test
    fun `starts in draft`() {
        val item = newCutoffBatch("code-453", "title-67")
        assertEquals(CutoffBatchStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newCutoffBatch("code-453", "title-67")
        try {
            item.moveTo(CutoffBatchStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CutoffBatchStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newCutoffBatch("code-453", "title-67")
        item.status = CutoffBatchStatus.CLOSED
        assertFails("move") { item.moveTo(CutoffBatchStatus.DRAFT) }
    }
}
