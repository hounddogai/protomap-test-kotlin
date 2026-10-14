package com.acme.transport.terminal.dwell.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackDwellRuleTest {
    @Test
    fun `starts in new`() {
        val item = newFallbackDwellRule("code-896", "title-973")
        assertEquals(FallbackDwellRuleStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newFallbackDwellRule("code-896", "title-973")
        try {
            item.moveTo(FallbackDwellRuleStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackDwellRuleStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newFallbackDwellRule("code-896", "title-973")
        item.status = FallbackDwellRuleStatus.FAILED
        assertFails("move") { item.moveTo(FallbackDwellRuleStatus.NEW) }
    }
}
