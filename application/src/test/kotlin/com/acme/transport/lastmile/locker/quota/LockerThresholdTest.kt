package com.acme.transport.lastmile.locker.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LockerThresholdTest {
    @Test
    fun `starts in new`() {
        val item = newLockerThreshold("code-991", "name-128")
        assertEquals(LockerThresholdStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newLockerThreshold("code-991", "name-128")
        try {
            item.moveTo(LockerThresholdStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LockerThresholdStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newLockerThreshold("code-991", "name-128")
        item.status = LockerThresholdStatus.FAILED
        assertFails("move") { item.moveTo(LockerThresholdStatus.NEW) }
    }
}
