package com.acme.transport.lastmile.redelivery.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryRedeliveryRuleTest {
    @Test
    fun `starts in idle`() {
        val item = newPrimaryRedeliveryRule("code-304", "name-746")
        assertEquals(PrimaryRedeliveryRuleStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newPrimaryRedeliveryRule("code-304", "name-746")
        try {
            item.moveTo(PrimaryRedeliveryRuleStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryRedeliveryRuleStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newPrimaryRedeliveryRule("code-304", "name-746")
        item.status = PrimaryRedeliveryRuleStatus.STOPPED
        assertFails("move") { item.moveTo(PrimaryRedeliveryRuleStatus.IDLE) }
    }
}
