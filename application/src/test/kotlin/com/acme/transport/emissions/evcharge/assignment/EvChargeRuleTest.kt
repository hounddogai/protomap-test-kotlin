package com.acme.transport.emissions.evcharge.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EvChargeRuleTest {
    @Test
    fun `starts in new`() {
        val item = newEvChargeRule("code-570", "name-556")
        assertEquals(EvChargeRuleStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newEvChargeRule("code-570", "name-556")
        try {
            item.moveTo(EvChargeRuleStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EvChargeRuleStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newEvChargeRule("code-570", "name-556")
        item.status = EvChargeRuleStatus.FAILED
        assertFails("move") { item.moveTo(EvChargeRuleStatus.NEW) }
    }
}
