package com.acme.transport.fleetops.defect.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefectScoreTest {
    @Test
    fun `starts in planned`() {
        val item = newDefectScore("code-172", "label-430")
        assertEquals(DefectScoreStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newDefectScore("code-172", "label-430")
        try {
            item.moveTo(DefectScoreStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefectScoreStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newDefectScore("code-172", "label-430")
        item.status = DefectScoreStatus.CANCELLED
        assertFails("move") { item.moveTo(DefectScoreStatus.PLANNED) }
    }
}
