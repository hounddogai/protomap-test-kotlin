package com.acme.transport.fleetops.inspection.inspectionscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedInspectionScoreTest {
    @Test
    fun `starts in draft`() {
        val item = newWeightedInspectionScore("code-256", "label-46")
        assertEquals(WeightedInspectionScoreStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newWeightedInspectionScore("code-256", "label-46")
        try {
            item.moveTo(WeightedInspectionScoreStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedInspectionScoreStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newWeightedInspectionScore("code-256", "label-46")
        item.status = WeightedInspectionScoreStatus.CLOSED
        assertFails("move") { item.moveTo(WeightedInspectionScoreStatus.DRAFT) }
    }
}
