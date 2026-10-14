package com.acme.transport.terminal.sortation.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalSortationAdjustmentTest {
    @Test
    fun `starts in planned`() {
        val item = newSeasonalSortationAdjustment("code-883", "name-260")
        assertEquals(SeasonalSortationAdjustmentStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newSeasonalSortationAdjustment("code-883", "name-260")
        try {
            item.moveTo(SeasonalSortationAdjustmentStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalSortationAdjustmentStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newSeasonalSortationAdjustment("code-883", "name-260")
        item.status = SeasonalSortationAdjustmentStatus.CANCELLED
        assertFails("move") { item.moveTo(SeasonalSortationAdjustmentStatus.PLANNED) }
    }
}
