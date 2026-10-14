package com.acme.transport.fleetops.tire.tireagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyTireAssignmentTest {
    @Test
    fun `starts in planned`() {
        val item = newLegacyTireAssignment("code-170", "title-543")
        assertEquals(LegacyTireAssignmentStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newLegacyTireAssignment("code-170", "title-543")
        try {
            item.moveTo(LegacyTireAssignmentStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyTireAssignmentStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newLegacyTireAssignment("code-170", "title-543")
        item.status = LegacyTireAssignmentStatus.CANCELLED
        assertFails("move") { item.moveTo(LegacyTireAssignmentStatus.PLANNED) }
    }
}
