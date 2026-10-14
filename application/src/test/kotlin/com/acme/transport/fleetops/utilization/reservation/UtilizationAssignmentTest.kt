package com.acme.transport.fleetops.utilization.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class UtilizationAssignmentTest {
    @Test
    fun `starts in new`() {
        val item = newUtilizationAssignment("code-97", "title-135")
        assertEquals(UtilizationAssignmentStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newUtilizationAssignment("code-97", "title-135")
        try {
            item.moveTo(UtilizationAssignmentStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(UtilizationAssignmentStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newUtilizationAssignment("code-97", "title-135")
        item.status = UtilizationAssignmentStatus.FAILED
        assertFails("move") { item.moveTo(UtilizationAssignmentStatus.NEW) }
    }
}
