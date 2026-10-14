package com.acme.transport.lastmile.locker.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LockerTargetTest {
    @Test
    fun `starts in planned`() {
        val item = newLockerTarget("code-125", "name-130")
        assertEquals(LockerTargetStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newLockerTarget("code-125", "name-130")
        try {
            item.moveTo(LockerTargetStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LockerTargetStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newLockerTarget("code-125", "name-130")
        item.status = LockerTargetStatus.CANCELLED
        assertFails("move") { item.moveTo(LockerTargetStatus.PLANNED) }
    }
}
