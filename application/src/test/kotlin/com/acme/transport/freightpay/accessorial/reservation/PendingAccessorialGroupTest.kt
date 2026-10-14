package com.acme.transport.freightpay.accessorial.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingAccessorialGroupTest {
    @Test
    fun `starts in planned`() {
        val item = newPendingAccessorialGroup("code-752", "name-333")
        assertEquals(PendingAccessorialGroupStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newPendingAccessorialGroup("code-752", "name-333")
        try {
            item.moveTo(PendingAccessorialGroupStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingAccessorialGroupStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newPendingAccessorialGroup("code-752", "name-333")
        item.status = PendingAccessorialGroupStatus.CANCELLED
        assertFails("move") { item.moveTo(PendingAccessorialGroupStatus.PLANNED) }
    }
}
