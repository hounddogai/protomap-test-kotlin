package com.acme.transport.lastmile.locker.lockerquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedLockerAssignmentTest {
    @Test
    fun `starts in created`() {
        val item = newCappedLockerAssignment("code-784", "title-313")
        assertEquals(CappedLockerAssignmentStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newCappedLockerAssignment("code-784", "title-313")
        try {
            item.moveTo(CappedLockerAssignmentStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedLockerAssignmentStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newCappedLockerAssignment("code-784", "title-313")
        item.status = CappedLockerAssignmentStatus.RETIRED
        assertFails("move") { item.moveTo(CappedLockerAssignmentStatus.CREATED) }
    }
}
