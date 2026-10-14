package com.acme.transport.linehaul.relay.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingRelayEntryTest {
    @Test
    fun `starts in planned`() {
        val item = newRollingRelayEntry("code-60", "name-593")
        assertEquals(RollingRelayEntryStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newRollingRelayEntry("code-60", "name-593")
        try {
            item.moveTo(RollingRelayEntryStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingRelayEntryStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newRollingRelayEntry("code-60", "name-593")
        item.status = RollingRelayEntryStatus.CANCELLED
        assertFails("move") { item.moveTo(RollingRelayEntryStatus.PLANNED) }
    }
}
