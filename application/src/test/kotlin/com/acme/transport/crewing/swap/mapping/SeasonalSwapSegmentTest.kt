package com.acme.transport.crewing.swap.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalSwapSegmentTest {
    @Test
    fun `starts in planned`() {
        val item = newSeasonalSwapSegment("code-42", "name-43")
        assertEquals(SeasonalSwapSegmentStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newSeasonalSwapSegment("code-42", "name-43")
        try {
            item.moveTo(SeasonalSwapSegmentStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalSwapSegmentStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newSeasonalSwapSegment("code-42", "name-43")
        item.status = SeasonalSwapSegmentStatus.CANCELLED
        assertFails("move") { item.moveTo(SeasonalSwapSegmentStatus.PLANNED) }
    }
}
