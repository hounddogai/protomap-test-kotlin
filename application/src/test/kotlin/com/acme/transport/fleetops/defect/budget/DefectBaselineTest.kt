package com.acme.transport.fleetops.defect.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefectBaselineTest {
    @Test
    fun `starts in planned`() {
        val item = newDefectBaseline("code-572", "label-334")
        assertEquals(DefectBaselineStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newDefectBaseline("code-572", "label-334")
        try {
            item.moveTo(DefectBaselineStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefectBaselineStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newDefectBaseline("code-572", "label-334")
        item.status = DefectBaselineStatus.CANCELLED
        assertFails("move") { item.moveTo(DefectBaselineStatus.PLANNED) }
    }
}
