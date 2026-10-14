package com.acme.transport.lastmile.locker.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LockerAssignmentTest {
    @Test
    fun `starts in created`() {
        val item = newLockerAssignment("code-768", "name-740")
        assertEquals(LockerAssignmentStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newLockerAssignment("code-768", "name-740")
        try {
            item.moveTo(LockerAssignmentStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LockerAssignmentStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newLockerAssignment("code-768", "name-740")
        item.status = LockerAssignmentStatus.RETIRED
        assertFails("move") { item.moveTo(LockerAssignmentStatus.CREATED) }
    }
}
