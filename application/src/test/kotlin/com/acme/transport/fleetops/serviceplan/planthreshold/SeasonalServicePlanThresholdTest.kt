package com.acme.transport.fleetops.serviceplan.planthreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalServicePlanThresholdTest {
    @Test
    fun `starts in new`() {
        val item = newSeasonalServicePlanThreshold("code-169", "label-790")
        assertEquals(SeasonalServicePlanThresholdStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newSeasonalServicePlanThreshold("code-169", "label-790")
        try {
            item.moveTo(SeasonalServicePlanThresholdStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalServicePlanThresholdStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newSeasonalServicePlanThreshold("code-169", "label-790")
        item.status = SeasonalServicePlanThresholdStatus.FAILED
        assertFails("move") { item.moveTo(SeasonalServicePlanThresholdStatus.NEW) }
    }
}
