package com.acme.transport.linehaul.trailer.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingTrailerAssignmentTest {
    @Test
    fun `starts in new`() {
        val item = newRollingTrailerAssignment("code-714", "name-281")
        assertEquals(RollingTrailerAssignmentStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newRollingTrailerAssignment("code-714", "name-281")
        try {
            item.moveTo(RollingTrailerAssignmentStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingTrailerAssignmentStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRollingTrailerAssignment("code-714", "name-281")
        item.status = RollingTrailerAssignmentStatus.FAILED
        assertFails("move") { item.moveTo(RollingTrailerAssignmentStatus.NEW) }
    }
}
