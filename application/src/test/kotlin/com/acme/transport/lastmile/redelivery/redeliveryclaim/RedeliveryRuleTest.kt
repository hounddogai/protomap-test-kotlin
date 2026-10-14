package com.acme.transport.lastmile.redelivery.redeliveryclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RedeliveryRuleTest {
    @Test
    fun `starts in idle`() {
        val item = newRedeliveryRule("code-454", "title-267")
        assertEquals(RedeliveryRuleStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newRedeliveryRule("code-454", "title-267")
        try {
            item.moveTo(RedeliveryRuleStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RedeliveryRuleStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRedeliveryRule("code-454", "title-267")
        item.status = RedeliveryRuleStatus.STOPPED
        assertFails("move") { item.moveTo(RedeliveryRuleStatus.IDLE) }
    }
}
