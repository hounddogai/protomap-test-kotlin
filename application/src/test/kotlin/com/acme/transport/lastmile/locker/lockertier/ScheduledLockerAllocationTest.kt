package com.acme.transport.lastmile.locker.lockertier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledLockerAllocationTest {
    @Test
    fun `starts in open`() {
        val item = newScheduledLockerAllocation("code-641", "label-918")
        assertEquals(ScheduledLockerAllocationStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newScheduledLockerAllocation("code-641", "label-918")
        try {
            item.moveTo(ScheduledLockerAllocationStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledLockerAllocationStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newScheduledLockerAllocation("code-641", "label-918")
        item.status = ScheduledLockerAllocationStatus.ARCHIVED
        assertFails("move") { item.moveTo(ScheduledLockerAllocationStatus.OPEN) }
    }
}
