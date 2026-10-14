package com.acme.transport.crewing.roster.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RosterPlanTest {
    @Test
    fun `starts in created`() {
        val item = newRosterPlan("code-9", "title-454")
        assertEquals(RosterPlanStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newRosterPlan("code-9", "title-454")
        try {
            item.moveTo(RosterPlanStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RosterPlanStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRosterPlan("code-9", "title-454")
        item.status = RosterPlanStatus.RETIRED
        assertFails("move") { item.moveTo(RosterPlanStatus.CREATED) }
    }
}
