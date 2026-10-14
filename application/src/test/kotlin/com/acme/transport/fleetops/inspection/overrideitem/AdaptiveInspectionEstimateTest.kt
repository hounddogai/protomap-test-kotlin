package com.acme.transport.fleetops.inspection.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveInspectionEstimateTest {
    @Test
    fun `starts in new`() {
        val item = newAdaptiveInspectionEstimate("code-739", "title-741")
        assertEquals(AdaptiveInspectionEstimateStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newAdaptiveInspectionEstimate("code-739", "title-741")
        try {
            item.moveTo(AdaptiveInspectionEstimateStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveInspectionEstimateStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newAdaptiveInspectionEstimate("code-739", "title-741")
        item.status = AdaptiveInspectionEstimateStatus.FAILED
        assertFails("move") { item.moveTo(AdaptiveInspectionEstimateStatus.NEW) }
    }
}
