package com.acme.transport.lastmile.locker.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LockerCycleTest {
    @Test
    fun `starts in planned`() {
        val item = newLockerCycle("code-640", "title-166")
        assertEquals(LockerCycleStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newLockerCycle("code-640", "title-166")
        try {
            item.moveTo(LockerCycleStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LockerCycleStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newLockerCycle("code-640", "title-166")
        item.status = LockerCycleStatus.CANCELLED
        assertFails("move") { item.moveTo(LockerCycleStatus.PLANNED) }
    }
}
