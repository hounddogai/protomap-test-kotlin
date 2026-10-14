package com.acme.transport.linehaul.backhaul.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BackhaulBatchTest {
    @Test
    fun `starts in open`() {
        val item = newBackhaulBatch("code-378", "label-784")
        assertEquals(BackhaulBatchStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newBackhaulBatch("code-378", "label-784")
        try {
            item.moveTo(BackhaulBatchStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BackhaulBatchStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newBackhaulBatch("code-378", "label-784")
        item.status = BackhaulBatchStatus.ARCHIVED
        assertFails("move") { item.moveTo(BackhaulBatchStatus.OPEN) }
    }
}
