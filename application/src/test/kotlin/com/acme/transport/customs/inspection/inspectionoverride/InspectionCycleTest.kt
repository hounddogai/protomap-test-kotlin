package com.acme.transport.customs.inspection.inspectionoverride

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionCycleTest {
    @Test
    fun `starts in open`() {
        val item = newInspectionCycle("code-566", "label-444")
        assertEquals(InspectionCycleStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newInspectionCycle("code-566", "label-444")
        try {
            item.moveTo(InspectionCycleStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionCycleStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newInspectionCycle("code-566", "label-444")
        item.status = InspectionCycleStatus.ARCHIVED
        assertFails("move") { item.moveTo(InspectionCycleStatus.OPEN) }
    }
}
