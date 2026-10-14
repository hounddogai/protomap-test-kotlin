package com.acme.transport.crewing.roster.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingRosterPlanTest {
    @Test
    fun `starts in created`() {
        val item = newRollingRosterPlan("code-882", "title-316")
        assertEquals(RollingRosterPlanStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newRollingRosterPlan("code-882", "title-316")
        try {
            item.moveTo(RollingRosterPlanStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingRosterPlanStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRollingRosterPlan("code-882", "title-316")
        item.status = RollingRosterPlanStatus.RETIRED
        assertFails("move") { item.moveTo(RollingRosterPlanStatus.CREATED) }
    }
}
