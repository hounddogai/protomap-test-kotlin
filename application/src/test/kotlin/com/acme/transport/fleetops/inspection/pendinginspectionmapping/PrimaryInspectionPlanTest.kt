package com.acme.transport.fleetops.inspection.pendinginspectionmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryInspectionPlanTest {
    @Test
    fun `starts in open`() {
        val item = newPrimaryInspectionPlan("code-596", "label-33")
        assertEquals(PrimaryInspectionPlanStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newPrimaryInspectionPlan("code-596", "label-33")
        try {
            item.moveTo(PrimaryInspectionPlanStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryInspectionPlanStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newPrimaryInspectionPlan("code-596", "label-33")
        item.status = PrimaryInspectionPlanStatus.ARCHIVED
        assertFails("move") { item.moveTo(PrimaryInspectionPlanStatus.OPEN) }
    }
}
