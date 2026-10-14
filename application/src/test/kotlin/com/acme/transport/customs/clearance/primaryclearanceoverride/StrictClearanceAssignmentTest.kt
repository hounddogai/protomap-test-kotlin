package com.acme.transport.customs.clearance.primaryclearanceoverride

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictClearanceAssignmentTest {
    @Test
    fun `starts in new`() {
        val item = newStrictClearanceAssignment("code-485", "label-715")
        assertEquals(StrictClearanceAssignmentStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newStrictClearanceAssignment("code-485", "label-715")
        try {
            item.moveTo(StrictClearanceAssignmentStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictClearanceAssignmentStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newStrictClearanceAssignment("code-485", "label-715")
        item.status = StrictClearanceAssignmentStatus.FAILED
        assertFails("move") { item.moveTo(StrictClearanceAssignmentStatus.NEW) }
    }
}
