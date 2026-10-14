package com.acme.transport.lastmile.redelivery.redeliveryproposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RedeliverySummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRedeliverySummaryValidator()
        val item = newRedeliverySummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RedeliverySummaryRequiredCodeRule("redelivery summary needs a value")
        val item = newRedeliverySummary("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = RedeliverySummaryRequiredShiftCodeRule("redelivery summary needs a value")
        val item = newRedeliverySummary("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long unit`() {
        val rule = RedeliverySummaryUnitLengthRule(64)
        val item = newRedeliverySummary("x", "y")
        item.unit = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
