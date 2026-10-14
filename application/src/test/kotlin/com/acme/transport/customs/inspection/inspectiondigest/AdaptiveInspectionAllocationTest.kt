package com.acme.transport.customs.inspection.inspectiondigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveInspectionAllocationTest {
    @Test
    fun `starts in created`() {
        val item = newAdaptiveInspectionAllocation("code-144", "name-225")
        assertEquals(AdaptiveInspectionAllocationStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newAdaptiveInspectionAllocation("code-144", "name-225")
        try {
            item.moveTo(AdaptiveInspectionAllocationStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveInspectionAllocationStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newAdaptiveInspectionAllocation("code-144", "name-225")
        item.status = AdaptiveInspectionAllocationStatus.RETIRED
        assertFails("move") { item.moveTo(AdaptiveInspectionAllocationStatus.CREATED) }
    }
}
