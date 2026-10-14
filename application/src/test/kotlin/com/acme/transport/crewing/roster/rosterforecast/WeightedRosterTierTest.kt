package com.acme.transport.crewing.roster.rosterforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedRosterTierTest {
    @Test
    fun `starts in proposed`() {
        val item = newWeightedRosterTier("code-143", "title-778")
        assertEquals(WeightedRosterTierStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newWeightedRosterTier("code-143", "title-778")
        try {
            item.moveTo(WeightedRosterTierStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedRosterTierStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newWeightedRosterTier("code-143", "title-778")
        item.status = WeightedRosterTierStatus.WITHDRAWN
        assertFails("move") { item.moveTo(WeightedRosterTierStatus.PROPOSED) }
    }
}
