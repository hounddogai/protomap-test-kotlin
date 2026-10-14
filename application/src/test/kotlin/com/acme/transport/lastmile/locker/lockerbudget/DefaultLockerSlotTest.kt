package com.acme.transport.lastmile.locker.lockerbudget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultLockerSlotTest {
    @Test
    fun `starts in new`() {
        val item = newDefaultLockerSlot("code-362", "name-365")
        assertEquals(DefaultLockerSlotStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newDefaultLockerSlot("code-362", "name-365")
        try {
            item.moveTo(DefaultLockerSlotStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultLockerSlotStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newDefaultLockerSlot("code-362", "name-365")
        item.status = DefaultLockerSlotStatus.FAILED
        assertFails("move") { item.moveTo(DefaultLockerSlotStatus.NEW) }
    }
}
