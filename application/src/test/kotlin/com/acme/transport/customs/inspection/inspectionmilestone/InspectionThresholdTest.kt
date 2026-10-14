package com.acme.transport.customs.inspection.inspectionmilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionThresholdTest {
    @Test
    fun `starts in proposed`() {
        val item = newInspectionThreshold("code-959", "name-980")
        assertEquals(InspectionThresholdStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newInspectionThreshold("code-959", "name-980")
        try {
            item.moveTo(InspectionThresholdStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionThresholdStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newInspectionThreshold("code-959", "name-980")
        item.status = InspectionThresholdStatus.WITHDRAWN
        assertFails("move") { item.moveTo(InspectionThresholdStatus.PROPOSED) }
    }
}
