package com.acme.transport.safety.violation.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientViolationAssignmentTest {
    @Test
    fun `starts in created`() {
        val item = newLenientViolationAssignment("code-459", "label-834")
        assertEquals(LenientViolationAssignmentStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newLenientViolationAssignment("code-459", "label-834")
        try {
            item.moveTo(LenientViolationAssignmentStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientViolationAssignmentStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newLenientViolationAssignment("code-459", "label-834")
        item.status = LenientViolationAssignmentStatus.RETIRED
        assertFails("move") { item.moveTo(LenientViolationAssignmentStatus.CREATED) }
    }
}
