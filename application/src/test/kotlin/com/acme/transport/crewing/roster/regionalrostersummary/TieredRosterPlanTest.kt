package com.acme.transport.crewing.roster.regionalrostersummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredRosterPlanTest {
    @Test
    fun `starts in idle`() {
        val item = newTieredRosterPlan("code-373", "name-924")
        assertEquals(TieredRosterPlanStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newTieredRosterPlan("code-373", "name-924")
        try {
            item.moveTo(TieredRosterPlanStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredRosterPlanStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newTieredRosterPlan("code-373", "name-924")
        item.status = TieredRosterPlanStatus.STOPPED
        assertFails("move") { item.moveTo(TieredRosterPlanStatus.IDLE) }
    }
}
