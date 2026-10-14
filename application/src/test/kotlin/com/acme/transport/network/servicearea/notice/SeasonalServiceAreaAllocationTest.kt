package com.acme.transport.network.servicearea.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalServiceAreaAllocationTest {
    @Test
    fun `starts in planned`() {
        val item = newSeasonalServiceAreaAllocation("code-164", "title-600")
        assertEquals(SeasonalServiceAreaAllocationStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newSeasonalServiceAreaAllocation("code-164", "title-600")
        try {
            item.moveTo(SeasonalServiceAreaAllocationStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalServiceAreaAllocationStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newSeasonalServiceAreaAllocation("code-164", "title-600")
        item.status = SeasonalServiceAreaAllocationStatus.CANCELLED
        assertFails("move") { item.moveTo(SeasonalServiceAreaAllocationStatus.PLANNED) }
    }
}
