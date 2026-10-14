package com.acme.transport.safety.violation.violationquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ViolationAssignmentTest {
    @Test
    fun `starts in created`() {
        val item = newViolationAssignment("code-109", "label-784")
        assertEquals(ViolationAssignmentStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newViolationAssignment("code-109", "label-784")
        try {
            item.moveTo(ViolationAssignmentStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ViolationAssignmentStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newViolationAssignment("code-109", "label-784")
        item.status = ViolationAssignmentStatus.RETIRED
        assertFails("move") { item.moveTo(ViolationAssignmentStatus.CREATED) }
    }
}
