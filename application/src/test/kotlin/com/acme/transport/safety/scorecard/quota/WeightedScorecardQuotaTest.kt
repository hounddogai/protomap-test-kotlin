package com.acme.transport.safety.scorecard.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedScorecardQuotaTest {
    @Test
    fun `starts in draft`() {
        val item = newWeightedScorecardQuota("code-5", "name-820")
        assertEquals(WeightedScorecardQuotaStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newWeightedScorecardQuota("code-5", "name-820")
        try {
            item.moveTo(WeightedScorecardQuotaStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedScorecardQuotaStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newWeightedScorecardQuota("code-5", "name-820")
        item.status = WeightedScorecardQuotaStatus.CLOSED
        assertFails("move") { item.moveTo(WeightedScorecardQuotaStatus.DRAFT) }
    }
}
