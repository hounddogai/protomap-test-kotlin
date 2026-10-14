package com.acme.transport.terminal.crossdock.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CrossDockBatchTest {
    @Test
    fun `starts in open`() {
        val item = newCrossDockBatch("code-367", "name-533")
        assertEquals(CrossDockBatchStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newCrossDockBatch("code-367", "name-533")
        try {
            item.moveTo(CrossDockBatchStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CrossDockBatchStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newCrossDockBatch("code-367", "name-533")
        item.status = CrossDockBatchStatus.ARCHIVED
        assertFails("move") { item.moveTo(CrossDockBatchStatus.OPEN) }
    }
}
