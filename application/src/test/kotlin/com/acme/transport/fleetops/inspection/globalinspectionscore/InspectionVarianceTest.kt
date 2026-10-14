package com.acme.transport.fleetops.inspection.globalinspectionscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionVarianceTest {
    @Test
    fun `starts in open`() {
        val item = newInspectionVariance("code-415", "title-666")
        assertEquals(InspectionVarianceStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newInspectionVariance("code-415", "title-666")
        try {
            item.moveTo(InspectionVarianceStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionVarianceStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newInspectionVariance("code-415", "title-666")
        item.status = InspectionVarianceStatus.ARCHIVED
        assertFails("move") { item.moveTo(InspectionVarianceStatus.OPEN) }
    }
}
