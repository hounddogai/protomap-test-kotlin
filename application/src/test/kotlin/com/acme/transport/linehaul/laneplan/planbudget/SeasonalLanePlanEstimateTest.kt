package com.acme.transport.linehaul.laneplan.planbudget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalLanePlanEstimateTest {
    @Test
    fun `starts in new`() {
        val item = newSeasonalLanePlanEstimate("code-541", "name-434")
        assertEquals(SeasonalLanePlanEstimateStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newSeasonalLanePlanEstimate("code-541", "name-434")
        try {
            item.moveTo(SeasonalLanePlanEstimateStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalLanePlanEstimateStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newSeasonalLanePlanEstimate("code-541", "name-434")
        item.status = SeasonalLanePlanEstimateStatus.FAILED
        assertFails("move") { item.moveTo(SeasonalLanePlanEstimateStatus.NEW) }
    }
}
