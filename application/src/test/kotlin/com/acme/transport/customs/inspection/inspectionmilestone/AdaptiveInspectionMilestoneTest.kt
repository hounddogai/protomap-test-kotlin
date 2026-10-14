package com.acme.transport.customs.inspection.inspectionmilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveInspectionMilestoneTest {
    @Test
    fun `starts in created`() {
        val item = newAdaptiveInspectionMilestone("code-436", "label-828")
        assertEquals(AdaptiveInspectionMilestoneStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newAdaptiveInspectionMilestone("code-436", "label-828")
        try {
            item.moveTo(AdaptiveInspectionMilestoneStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveInspectionMilestoneStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newAdaptiveInspectionMilestone("code-436", "label-828")
        item.status = AdaptiveInspectionMilestoneStatus.RETIRED
        assertFails("move") { item.moveTo(AdaptiveInspectionMilestoneStatus.CREATED) }
    }
}
