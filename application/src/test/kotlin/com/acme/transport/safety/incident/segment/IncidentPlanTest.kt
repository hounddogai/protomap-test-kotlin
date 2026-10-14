package com.acme.transport.safety.incident.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class IncidentPlanTest {
    @Test
    fun `starts in created`() {
        val item = newIncidentPlan("code-909", "label-408")
        assertEquals(IncidentPlanStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newIncidentPlan("code-909", "label-408")
        try {
            item.moveTo(IncidentPlanStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(IncidentPlanStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newIncidentPlan("code-909", "label-408")
        item.status = IncidentPlanStatus.RETIRED
        assertFails("move") { item.moveTo(IncidentPlanStatus.CREATED) }
    }
}
