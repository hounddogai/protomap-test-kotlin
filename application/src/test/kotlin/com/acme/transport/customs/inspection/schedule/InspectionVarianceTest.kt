package com.acme.transport.customs.inspection.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionVarianceTest {
    @Test
    fun `starts in planned`() {
        val item = newInspectionVariance("code-559", "name-902")
        assertEquals(InspectionVarianceStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newInspectionVariance("code-559", "name-902")
        try {
            item.moveTo(InspectionVarianceStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionVarianceStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newInspectionVariance("code-559", "name-902")
        item.status = InspectionVarianceStatus.CANCELLED
        assertFails("move") { item.moveTo(InspectionVarianceStatus.PLANNED) }
    }
}
