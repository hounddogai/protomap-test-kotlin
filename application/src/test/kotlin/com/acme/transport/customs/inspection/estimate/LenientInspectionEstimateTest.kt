package com.acme.transport.customs.inspection.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientInspectionEstimateTest {
    @Test
    fun `starts in proposed`() {
        val item = newLenientInspectionEstimate("code-945", "title-390")
        assertEquals(LenientInspectionEstimateStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newLenientInspectionEstimate("code-945", "title-390")
        try {
            item.moveTo(LenientInspectionEstimateStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientInspectionEstimateStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newLenientInspectionEstimate("code-945", "title-390")
        item.status = LenientInspectionEstimateStatus.WITHDRAWN
        assertFails("move") { item.moveTo(LenientInspectionEstimateStatus.PROPOSED) }
    }
}
