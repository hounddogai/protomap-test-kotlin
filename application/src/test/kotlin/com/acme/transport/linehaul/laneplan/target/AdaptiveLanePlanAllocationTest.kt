package com.acme.transport.linehaul.laneplan.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveLanePlanAllocationTest {
    @Test
    fun `starts in created`() {
        val item = newAdaptiveLanePlanAllocation("code-25", "title-757")
        assertEquals(AdaptiveLanePlanAllocationStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newAdaptiveLanePlanAllocation("code-25", "title-757")
        try {
            item.moveTo(AdaptiveLanePlanAllocationStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveLanePlanAllocationStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newAdaptiveLanePlanAllocation("code-25", "title-757")
        item.status = AdaptiveLanePlanAllocationStatus.RETIRED
        assertFails("move") { item.moveTo(AdaptiveLanePlanAllocationStatus.CREATED) }
    }
}
