package com.acme.transport.fleetops.serviceplan.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualServicePlanAssignmentTest {
    @Test
    fun `starts in created`() {
        val item = newManualServicePlanAssignment("code-611", "title-258")
        assertEquals(ManualServicePlanAssignmentStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newManualServicePlanAssignment("code-611", "title-258")
        try {
            item.moveTo(ManualServicePlanAssignmentStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualServicePlanAssignmentStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newManualServicePlanAssignment("code-611", "title-258")
        item.status = ManualServicePlanAssignmentStatus.RETIRED
        assertFails("move") { item.moveTo(ManualServicePlanAssignmentStatus.CREATED) }
    }
}
