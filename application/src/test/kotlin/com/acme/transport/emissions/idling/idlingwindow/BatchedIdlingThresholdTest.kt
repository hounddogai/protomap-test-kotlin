package com.acme.transport.emissions.idling.idlingwindow

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedIdlingThresholdTest {
    @Test
    fun `starts in planned`() {
        val item = newBatchedIdlingThreshold("code-980", "name-808")
        assertEquals(BatchedIdlingThresholdStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newBatchedIdlingThreshold("code-980", "name-808")
        try {
            item.moveTo(BatchedIdlingThresholdStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedIdlingThresholdStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newBatchedIdlingThreshold("code-980", "name-808")
        item.status = BatchedIdlingThresholdStatus.CANCELLED
        assertFails("move") { item.moveTo(BatchedIdlingThresholdStatus.PLANNED) }
    }
}
