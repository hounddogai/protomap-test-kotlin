package com.acme.transport.lastmile.redelivery.redeliveryrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledRedeliveryRuleTest {
    @Test
    fun `starts in created`() {
        val item = newScheduledRedeliveryRule("code-157", "name-569")
        assertEquals(ScheduledRedeliveryRuleStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newScheduledRedeliveryRule("code-157", "name-569")
        try {
            item.moveTo(ScheduledRedeliveryRuleStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledRedeliveryRuleStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newScheduledRedeliveryRule("code-157", "name-569")
        item.status = ScheduledRedeliveryRuleStatus.RETIRED
        assertFails("move") { item.moveTo(ScheduledRedeliveryRuleStatus.CREATED) }
    }
}
