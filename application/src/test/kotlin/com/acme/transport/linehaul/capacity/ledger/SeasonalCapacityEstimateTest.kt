package com.acme.transport.linehaul.capacity.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalCapacityEstimateTest {
    @Test
    fun `starts in open`() {
        val item = newSeasonalCapacityEstimate("code-492", "name-428")
        assertEquals(SeasonalCapacityEstimateStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newSeasonalCapacityEstimate("code-492", "name-428")
        try {
            item.moveTo(SeasonalCapacityEstimateStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalCapacityEstimateStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newSeasonalCapacityEstimate("code-492", "name-428")
        item.status = SeasonalCapacityEstimateStatus.ARCHIVED
        assertFails("move") { item.moveTo(SeasonalCapacityEstimateStatus.OPEN) }
    }
}
