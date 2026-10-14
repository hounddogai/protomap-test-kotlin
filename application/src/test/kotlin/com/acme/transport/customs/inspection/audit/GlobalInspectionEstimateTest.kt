package com.acme.transport.customs.inspection.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalInspectionEstimateTest {
    @Test
    fun `starts in proposed`() {
        val item = newGlobalInspectionEstimate("code-106", "title-401")
        assertEquals(GlobalInspectionEstimateStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newGlobalInspectionEstimate("code-106", "title-401")
        try {
            item.moveTo(GlobalInspectionEstimateStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalInspectionEstimateStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newGlobalInspectionEstimate("code-106", "title-401")
        item.status = GlobalInspectionEstimateStatus.WITHDRAWN
        assertFails("move") { item.moveTo(GlobalInspectionEstimateStatus.PROPOSED) }
    }
}
