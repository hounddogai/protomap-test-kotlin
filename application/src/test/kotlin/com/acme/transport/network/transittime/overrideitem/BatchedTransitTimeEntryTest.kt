package com.acme.transport.network.transittime.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedTransitTimeEntryTest {
    @Test
    fun `starts in open`() {
        val item = newBatchedTransitTimeEntry("code-724", "title-847")
        assertEquals(BatchedTransitTimeEntryStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newBatchedTransitTimeEntry("code-724", "title-847")
        try {
            item.moveTo(BatchedTransitTimeEntryStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedTransitTimeEntryStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newBatchedTransitTimeEntry("code-724", "title-847")
        item.status = BatchedTransitTimeEntryStatus.ARCHIVED
        assertFails("move") { item.moveTo(BatchedTransitTimeEntryStatus.OPEN) }
    }
}
