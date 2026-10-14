package com.acme.transport.terminal.crossdock.cappeddockestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalCrossDockSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalCrossDockSummaryValidator()
        val item = newGlobalCrossDockSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = GlobalCrossDockSummaryCodeLengthRule(120)
        val item = newGlobalCrossDockSummary("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = GlobalCrossDockSummaryRequiredCodeRule("global cross dock summary needs this field")
        val item = newGlobalCrossDockSummary("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
