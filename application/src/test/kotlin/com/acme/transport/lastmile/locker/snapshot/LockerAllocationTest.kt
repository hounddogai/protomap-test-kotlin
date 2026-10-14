package com.acme.transport.lastmile.locker.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LockerAllocationTest {
    @Test
    fun `starts in new`() {
        val item = newLockerAllocation("code-861", "name-498")
        assertEquals(LockerAllocationStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newLockerAllocation("code-861", "name-498")
        try {
            item.moveTo(LockerAllocationStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LockerAllocationStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newLockerAllocation("code-861", "name-498")
        item.status = LockerAllocationStatus.FAILED
        assertFails("move") { item.moveTo(LockerAllocationStatus.NEW) }
    }
}
