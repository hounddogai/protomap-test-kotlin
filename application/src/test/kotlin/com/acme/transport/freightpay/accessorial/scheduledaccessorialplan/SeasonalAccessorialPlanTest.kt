package com.acme.transport.freightpay.accessorial.scheduledaccessorialplan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalAccessorialPlanTest {
    @Test
    fun `starts in idle`() {
        val item = newSeasonalAccessorialPlan("code-595", "label-463")
        assertEquals(SeasonalAccessorialPlanStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newSeasonalAccessorialPlan("code-595", "label-463")
        try {
            item.moveTo(SeasonalAccessorialPlanStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalAccessorialPlanStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newSeasonalAccessorialPlan("code-595", "label-463")
        item.status = SeasonalAccessorialPlanStatus.STOPPED
        assertFails("move") { item.moveTo(SeasonalAccessorialPlanStatus.IDLE) }
    }
}
