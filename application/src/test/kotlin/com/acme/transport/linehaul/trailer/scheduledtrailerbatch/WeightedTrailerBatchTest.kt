package com.acme.transport.linehaul.trailer.scheduledtrailerbatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedTrailerBatchTest {
    @Test
    fun `starts in draft`() {
        val item = newWeightedTrailerBatch("code-404", "title-121")
        assertEquals(WeightedTrailerBatchStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newWeightedTrailerBatch("code-404", "title-121")
        try {
            item.moveTo(WeightedTrailerBatchStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedTrailerBatchStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newWeightedTrailerBatch("code-404", "title-121")
        item.status = WeightedTrailerBatchStatus.CLOSED
        assertFails("move") { item.moveTo(WeightedTrailerBatchStatus.DRAFT) }
    }
}
