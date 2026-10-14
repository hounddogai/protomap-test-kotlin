package com.acme.transport.lastmile.redelivery.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RedeliveryWindowTest {
    @Test
    fun `starts in planned`() {
        val item = newRedeliveryWindow("code-22", "label-155")
        assertEquals(RedeliveryWindowStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newRedeliveryWindow("code-22", "label-155")
        try {
            item.moveTo(RedeliveryWindowStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RedeliveryWindowStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newRedeliveryWindow("code-22", "label-155")
        item.status = RedeliveryWindowStatus.CANCELLED
        assertFails("move") { item.moveTo(RedeliveryWindowStatus.PLANNED) }
    }
}
