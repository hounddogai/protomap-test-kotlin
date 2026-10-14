package com.acme.transport.linehaul.tender.tenderbatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalTenderBatchTest {
    @Test
    fun `starts in planned`() {
        val item = newSeasonalTenderBatch("code-101", "title-655")
        assertEquals(SeasonalTenderBatchStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newSeasonalTenderBatch("code-101", "title-655")
        try {
            item.moveTo(SeasonalTenderBatchStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalTenderBatchStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newSeasonalTenderBatch("code-101", "title-655")
        item.status = SeasonalTenderBatchStatus.CANCELLED
        assertFails("move") { item.moveTo(SeasonalTenderBatchStatus.PLANNED) }
    }
}
