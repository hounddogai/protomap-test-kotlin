package com.acme.transport.customs.inspection.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultInspectionEstimateTest {
    @Test
    fun `starts in planned`() {
        val item = newDefaultInspectionEstimate("code-873", "name-226")
        assertEquals(DefaultInspectionEstimateStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newDefaultInspectionEstimate("code-873", "name-226")
        try {
            item.moveTo(DefaultInspectionEstimateStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultInspectionEstimateStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newDefaultInspectionEstimate("code-873", "name-226")
        item.status = DefaultInspectionEstimateStatus.CANCELLED
        assertFails("move") { item.moveTo(DefaultInspectionEstimateStatus.PLANNED) }
    }
}
