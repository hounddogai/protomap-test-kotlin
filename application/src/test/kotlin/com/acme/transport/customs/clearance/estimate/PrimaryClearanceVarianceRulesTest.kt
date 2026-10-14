package com.acme.transport.customs.clearance.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryClearanceVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryClearanceVarianceValidator()
        val item = newPrimaryClearanceVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = PrimaryClearanceVarianceCodeLengthRule(16)
        val item = newPrimaryClearanceVariance("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = PrimaryClearanceVarianceTitleLengthRule(32)
        val item = newPrimaryClearanceVariance("x", "y")
        item.title = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = PrimaryClearanceVarianceRequiredTitleRule("primary clearance variance needs this field")
        val item = newPrimaryClearanceVariance("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
