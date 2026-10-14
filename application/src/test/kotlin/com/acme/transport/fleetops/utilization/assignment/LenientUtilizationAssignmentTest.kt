package com.acme.transport.fleetops.utilization.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientUtilizationAssignmentTest {
    @Test
    fun `starts in draft`() {
        val item = newLenientUtilizationAssignment("code-931", "label-187")
        assertEquals(LenientUtilizationAssignmentStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newLenientUtilizationAssignment("code-931", "label-187")
        try {
            item.moveTo(LenientUtilizationAssignmentStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientUtilizationAssignmentStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newLenientUtilizationAssignment("code-931", "label-187")
        item.status = LenientUtilizationAssignmentStatus.CLOSED
        assertFails("move") { item.moveTo(LenientUtilizationAssignmentStatus.DRAFT) }
    }
}
