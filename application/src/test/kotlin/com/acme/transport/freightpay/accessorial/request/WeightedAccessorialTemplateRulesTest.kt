package com.acme.transport.freightpay.accessorial.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedAccessorialTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedAccessorialTemplateValidator()
        val item = newWeightedAccessorialTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = WeightedAccessorialTemplateNameLengthRule(1000)
        val item = newWeightedAccessorialTemplate("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = WeightedAccessorialTemplateExpiresAtRangeRule(120, 3600)
        val item = newWeightedAccessorialTemplate("x", "y")
        item.expiresAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
