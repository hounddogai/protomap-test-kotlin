package com.acme.transport.lastmile.locker.lockerquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictLockerThresholdTest {
    @Test
    fun `starts in open`() {
        val item = newStrictLockerThreshold("code-178", "name-823")
        assertEquals(StrictLockerThresholdStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newStrictLockerThreshold("code-178", "name-823")
        try {
            item.moveTo(StrictLockerThresholdStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictLockerThresholdStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newStrictLockerThreshold("code-178", "name-823")
        item.status = StrictLockerThresholdStatus.ARCHIVED
        assertFails("move") { item.moveTo(StrictLockerThresholdStatus.OPEN) }
    }
}
