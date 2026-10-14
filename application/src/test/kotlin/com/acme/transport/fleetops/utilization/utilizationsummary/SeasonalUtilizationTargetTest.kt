package com.acme.transport.fleetops.utilization.utilizationsummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalUtilizationTargetTest {
    @Test
    fun `starts in proposed`() {
        val item = newSeasonalUtilizationTarget("code-404", "label-12")
        assertEquals(SeasonalUtilizationTargetStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newSeasonalUtilizationTarget("code-404", "label-12")
        try {
            item.moveTo(SeasonalUtilizationTargetStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalUtilizationTargetStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newSeasonalUtilizationTarget("code-404", "label-12")
        item.status = SeasonalUtilizationTargetStatus.WITHDRAWN
        assertFails("move") { item.moveTo(SeasonalUtilizationTargetStatus.PROPOSED) }
    }
}
