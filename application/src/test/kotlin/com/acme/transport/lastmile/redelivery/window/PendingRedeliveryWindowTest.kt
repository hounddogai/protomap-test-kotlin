package com.acme.transport.lastmile.redelivery.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingRedeliveryWindowTest {
    @Test
    fun `starts in requested`() {
        val item = newPendingRedeliveryWindow("code-570", "name-160")
        assertEquals(PendingRedeliveryWindowStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newPendingRedeliveryWindow("code-570", "name-160")
        try {
            item.moveTo(PendingRedeliveryWindowStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingRedeliveryWindowStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newPendingRedeliveryWindow("code-570", "name-160")
        item.status = PendingRedeliveryWindowStatus.EXPIRED
        assertFails("move") { item.moveTo(PendingRedeliveryWindowStatus.REQUESTED) }
    }
}
