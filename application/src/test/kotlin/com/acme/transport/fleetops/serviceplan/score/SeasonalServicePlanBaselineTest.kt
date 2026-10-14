package com.acme.transport.fleetops.serviceplan.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalServicePlanBaselineTest {
    @Test
    fun `starts in draft`() {
        val item = newSeasonalServicePlanBaseline("code-500", "label-54")
        assertEquals(SeasonalServicePlanBaselineStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newSeasonalServicePlanBaseline("code-500", "label-54")
        try {
            item.moveTo(SeasonalServicePlanBaselineStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalServicePlanBaselineStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newSeasonalServicePlanBaseline("code-500", "label-54")
        item.status = SeasonalServicePlanBaselineStatus.CLOSED
        assertFails("move") { item.moveTo(SeasonalServicePlanBaselineStatus.DRAFT) }
    }
}
