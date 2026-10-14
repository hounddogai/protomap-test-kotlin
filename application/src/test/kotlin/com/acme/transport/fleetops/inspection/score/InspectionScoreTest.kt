package com.acme.transport.fleetops.inspection.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionScoreTest {
    @Test
    fun `starts in new`() {
        val item = newInspectionScore("code-711", "label-392")
        assertEquals(InspectionScoreStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newInspectionScore("code-711", "label-392")
        try {
            item.moveTo(InspectionScoreStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionScoreStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newInspectionScore("code-711", "label-392")
        item.status = InspectionScoreStatus.FAILED
        assertFails("move") { item.moveTo(InspectionScoreStatus.NEW) }
    }
}
