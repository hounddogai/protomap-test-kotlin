package com.acme.transport.crewing.roster.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingRosterPlanTest {
    @Test
    fun `starts in created`() {
        val item = newPendingRosterPlan("code-282", "name-172")
        assertEquals(PendingRosterPlanStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newPendingRosterPlan("code-282", "name-172")
        try {
            item.moveTo(PendingRosterPlanStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingRosterPlanStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newPendingRosterPlan("code-282", "name-172")
        item.status = PendingRosterPlanStatus.RETIRED
        assertFails("move") { item.moveTo(PendingRosterPlanStatus.CREATED) }
    }
}
