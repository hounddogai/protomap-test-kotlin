package com.acme.transport.linehaul.tender.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalTenderAssignmentTest {
    @Test
    fun `starts in new`() {
        val item = newSeasonalTenderAssignment("code-984", "label-263")
        assertEquals(SeasonalTenderAssignmentStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newSeasonalTenderAssignment("code-984", "label-263")
        try {
            item.moveTo(SeasonalTenderAssignmentStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalTenderAssignmentStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newSeasonalTenderAssignment("code-984", "label-263")
        item.status = SeasonalTenderAssignmentStatus.FAILED
        assertFails("move") { item.moveTo(SeasonalTenderAssignmentStatus.NEW) }
    }
}
