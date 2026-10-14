package com.acme.transport.customs.inspection.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionRollupTest {
    @Test
    fun `starts in planned`() {
        val item = newInspectionRollup("code-372", "label-358")
        assertEquals(InspectionRollupStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newInspectionRollup("code-372", "label-358")
        try {
            item.moveTo(InspectionRollupStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionRollupStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newInspectionRollup("code-372", "label-358")
        item.status = InspectionRollupStatus.CANCELLED
        assertFails("move") { item.moveTo(InspectionRollupStatus.PLANNED) }
    }
}
