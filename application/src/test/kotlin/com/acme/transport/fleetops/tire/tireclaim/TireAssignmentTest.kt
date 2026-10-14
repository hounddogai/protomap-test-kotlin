package com.acme.transport.fleetops.tire.tireclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TireAssignmentTest {
    @Test
    fun `starts in planned`() {
        val item = newTireAssignment("code-35", "title-403")
        assertEquals(TireAssignmentStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newTireAssignment("code-35", "title-403")
        try {
            item.moveTo(TireAssignmentStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TireAssignmentStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newTireAssignment("code-35", "title-403")
        item.status = TireAssignmentStatus.CANCELLED
        assertFails("move") { item.moveTo(TireAssignmentStatus.PLANNED) }
    }
}
