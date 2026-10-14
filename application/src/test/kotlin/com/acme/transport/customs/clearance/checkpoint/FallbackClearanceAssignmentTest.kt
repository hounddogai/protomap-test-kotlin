package com.acme.transport.customs.clearance.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackClearanceAssignmentTest {
    @Test
    fun `starts in created`() {
        val item = newFallbackClearanceAssignment("code-746", "label-666")
        assertEquals(FallbackClearanceAssignmentStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newFallbackClearanceAssignment("code-746", "label-666")
        try {
            item.moveTo(FallbackClearanceAssignmentStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackClearanceAssignmentStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newFallbackClearanceAssignment("code-746", "label-666")
        item.status = FallbackClearanceAssignmentStatus.RETIRED
        assertFails("move") { item.moveTo(FallbackClearanceAssignmentStatus.CREATED) }
    }
}
