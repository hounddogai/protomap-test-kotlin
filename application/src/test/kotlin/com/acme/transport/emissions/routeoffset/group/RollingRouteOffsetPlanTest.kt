package com.acme.transport.emissions.routeoffset.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingRouteOffsetPlanTest {
    @Test
    fun `starts in created`() {
        val item = newRollingRouteOffsetPlan("code-420", "label-636")
        assertEquals(RollingRouteOffsetPlanStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newRollingRouteOffsetPlan("code-420", "label-636")
        try {
            item.moveTo(RollingRouteOffsetPlanStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingRouteOffsetPlanStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRollingRouteOffsetPlan("code-420", "label-636")
        item.status = RollingRouteOffsetPlanStatus.RETIRED
        assertFails("move") { item.moveTo(RollingRouteOffsetPlanStatus.CREATED) }
    }
}
