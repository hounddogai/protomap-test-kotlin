package com.acme.transport.lastmile.redelivery.redeliveryrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledRedeliveryWindowTest {
    @Test
    fun `starts in draft`() {
        val item = newScheduledRedeliveryWindow("code-717", "name-554")
        assertEquals(ScheduledRedeliveryWindowStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newScheduledRedeliveryWindow("code-717", "name-554")
        try {
            item.moveTo(ScheduledRedeliveryWindowStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledRedeliveryWindowStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newScheduledRedeliveryWindow("code-717", "name-554")
        item.status = ScheduledRedeliveryWindowStatus.CLOSED
        assertFails("move") { item.moveTo(ScheduledRedeliveryWindowStatus.DRAFT) }
    }
}
