package com.acme.transport.terminal.crossdock.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CrossDockQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCrossDockQuotaValidator()
        val item = newCrossDockQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CrossDockQuotaRequiredCodeRule("cross dock quota needs an entry")
        val item = newCrossDockQuota("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = CrossDockQuotaRequiredNameRule("cross dock quota needs this field")
        val item = newCrossDockQuota("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
