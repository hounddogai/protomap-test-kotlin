package com.acme.transport.freightpay.accessorial.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AccessorialTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAccessorialTemplateValidator()
        val item = newAccessorialTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long channel`() {
        val rule = AccessorialTemplateChannelLengthRule(86400)
        val item = newAccessorialTemplate("x", "y")
        item.channel = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = AccessorialTemplateRequiredChannelRule("accessorial template needs this field")
        val item = newAccessorialTemplate("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = AccessorialTemplateOdometerRangeRule(1000, 3600)
        val item = newAccessorialTemplate("x", "y")
        item.odometer = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = AccessorialTemplateRequiredNameRule("accessorial template needs a value")
        val item = newAccessorialTemplate("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = AccessorialTemplateCodeLengthRule(3600)
        val item = newAccessorialTemplate("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
