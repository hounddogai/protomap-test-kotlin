package com.acme.transport.fleetops.inspection.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionThresholdTest {
    @Test
    fun `starts in proposed`() {
        val item = newInspectionThreshold("code-362", "title-897")
        assertEquals(InspectionThresholdStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newInspectionThreshold("code-362", "title-897")
        try {
            item.moveTo(InspectionThresholdStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionThresholdStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newInspectionThreshold("code-362", "title-897")
        item.status = InspectionThresholdStatus.WITHDRAWN
        assertFails("move") { item.moveTo(InspectionThresholdStatus.PROPOSED) }
    }
}
