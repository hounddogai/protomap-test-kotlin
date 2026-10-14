package com.acme.transport.safety.incident.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingIncidentPlanTest {
    @Test
    fun `starts in open`() {
        val item = newRollingIncidentPlan("code-98", "name-136")
        assertEquals(RollingIncidentPlanStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newRollingIncidentPlan("code-98", "name-136")
        try {
            item.moveTo(RollingIncidentPlanStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingIncidentPlanStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newRollingIncidentPlan("code-98", "name-136")
        item.status = RollingIncidentPlanStatus.ARCHIVED
        assertFails("move") { item.moveTo(RollingIncidentPlanStatus.OPEN) }
    }
}
