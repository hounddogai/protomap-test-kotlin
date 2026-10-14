package com.acme.transport.network.servicearea.areabatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedServiceAreaEntryTest {
    @Test
    fun `starts in planned`() {
        val item = newBatchedServiceAreaEntry("code-204", "label-753")
        assertEquals(BatchedServiceAreaEntryStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newBatchedServiceAreaEntry("code-204", "label-753")
        try {
            item.moveTo(BatchedServiceAreaEntryStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedServiceAreaEntryStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newBatchedServiceAreaEntry("code-204", "label-753")
        item.status = BatchedServiceAreaEntryStatus.CANCELLED
        assertFails("move") { item.moveTo(BatchedServiceAreaEntryStatus.PLANNED) }
    }
}
