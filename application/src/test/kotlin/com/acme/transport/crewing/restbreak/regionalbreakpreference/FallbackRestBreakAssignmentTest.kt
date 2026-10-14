package com.acme.transport.crewing.restbreak.regionalbreakpreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackRestBreakAssignmentTest {
    @Test
    fun `starts in proposed`() {
        val item = newFallbackRestBreakAssignment("code-504", "name-163")
        assertEquals(FallbackRestBreakAssignmentStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newFallbackRestBreakAssignment("code-504", "name-163")
        try {
            item.moveTo(FallbackRestBreakAssignmentStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackRestBreakAssignmentStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newFallbackRestBreakAssignment("code-504", "name-163")
        item.status = FallbackRestBreakAssignmentStatus.WITHDRAWN
        assertFails("move") { item.moveTo(FallbackRestBreakAssignmentStatus.PROPOSED) }
    }
}
