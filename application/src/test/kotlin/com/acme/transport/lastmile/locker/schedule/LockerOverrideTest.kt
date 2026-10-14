package com.acme.transport.lastmile.locker.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LockerOverrideTest {
    @Test
    fun `starts in planned`() {
        val item = newLockerOverride("code-738", "label-205")
        assertEquals(LockerOverrideStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newLockerOverride("code-738", "label-205")
        try {
            item.moveTo(LockerOverrideStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LockerOverrideStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newLockerOverride("code-738", "label-205")
        item.status = LockerOverrideStatus.CANCELLED
        assertFails("move") { item.moveTo(LockerOverrideStatus.PLANNED) }
    }
}
