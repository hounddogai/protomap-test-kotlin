package com.acme.transport.fleetops.inspection.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionGroupTest {
    @Test
    fun `starts in proposed`() {
        val item = newInspectionGroup("code-105", "name-413")
        assertEquals(InspectionGroupStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newInspectionGroup("code-105", "name-413")
        try {
            item.moveTo(InspectionGroupStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionGroupStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newInspectionGroup("code-105", "name-413")
        item.status = InspectionGroupStatus.WITHDRAWN
        assertFails("move") { item.moveTo(InspectionGroupStatus.PROPOSED) }
    }
}
