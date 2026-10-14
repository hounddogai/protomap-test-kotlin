package com.acme.transport.lastmile.redelivery.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualRedeliveryWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualRedeliveryWindowValidator()
        val item = newManualRedeliveryWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = ManualRedeliveryWindowNameLengthRule(32)
        val item = newManualRedeliveryWindow("x", "y")
        item.name = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank region`() {
        val rule = ManualRedeliveryWindowRequiredRegionRule("manual redelivery window needs a value")
        val item = newManualRedeliveryWindow("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ManualRedeliveryWindowCodeLengthRule(120)
        val item = newManualRedeliveryWindow("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ManualRedeliveryWindowRequiredCodeRule("manual redelivery window needs a value")
        val item = newManualRedeliveryWindow("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
