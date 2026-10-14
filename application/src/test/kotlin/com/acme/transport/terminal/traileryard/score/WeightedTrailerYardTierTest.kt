package com.acme.transport.terminal.traileryard.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedTrailerYardTierTest {
    @Test
    fun `starts in open`() {
        val item = newWeightedTrailerYardTier("code-220", "name-362")
        assertEquals(WeightedTrailerYardTierStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newWeightedTrailerYardTier("code-220", "name-362")
        try {
            item.moveTo(WeightedTrailerYardTierStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedTrailerYardTierStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newWeightedTrailerYardTier("code-220", "name-362")
        item.status = WeightedTrailerYardTierStatus.ARCHIVED
        assertFails("move") { item.moveTo(WeightedTrailerYardTierStatus.OPEN) }
    }
}
