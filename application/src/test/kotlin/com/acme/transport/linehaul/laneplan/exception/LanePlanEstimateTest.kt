package com.acme.transport.linehaul.laneplan.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LanePlanEstimateTest {
    @Test
    fun `starts in planned`() {
        val item = newLanePlanEstimate("code-425", "label-234")
        assertEquals(LanePlanEstimateStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newLanePlanEstimate("code-425", "label-234")
        try {
            item.moveTo(LanePlanEstimateStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LanePlanEstimateStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newLanePlanEstimate("code-425", "label-234")
        item.status = LanePlanEstimateStatus.CANCELLED
        assertFails("move") { item.moveTo(LanePlanEstimateStatus.PLANNED) }
    }
}
