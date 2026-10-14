package com.acme.transport.lastmile.redelivery.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RedeliveryBaselineTest {
    @Test
    fun `starts in idle`() {
        val item = newRedeliveryBaseline("code-217", "name-761")
        assertEquals(RedeliveryBaselineStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newRedeliveryBaseline("code-217", "name-761")
        try {
            item.moveTo(RedeliveryBaselineStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RedeliveryBaselineStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRedeliveryBaseline("code-217", "name-761")
        item.status = RedeliveryBaselineStatus.STOPPED
        assertFails("move") { item.moveTo(RedeliveryBaselineStatus.IDLE) }
    }
}
