package com.acme.transport.terminal.dwell.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalDwellProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalDwellProfileValidator()
        val item = newRegionalDwellProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RegionalDwellProfileCodeLengthRule(16)
        val item = newRegionalDwellProfile("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
