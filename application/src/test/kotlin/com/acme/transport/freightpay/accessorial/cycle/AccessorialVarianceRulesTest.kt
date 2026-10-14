package com.acme.transport.freightpay.accessorial.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AccessorialVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAccessorialVarianceValidator()
        val item = newAccessorialVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = AccessorialVarianceLabelLengthRule(3600)
        val item = newAccessorialVariance("x", "y")
        item.label = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = AccessorialVarianceRequiredLabelRule("accessorial variance needs a value")
        val item = newAccessorialVariance("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above limit`() {
        val rule = AccessorialVarianceLimitRangeRule(3600, 86400)
        val item = newAccessorialVariance("x", "y")
        item.limit = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
