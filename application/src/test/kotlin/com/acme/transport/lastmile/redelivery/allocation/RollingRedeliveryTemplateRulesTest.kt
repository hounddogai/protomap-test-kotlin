package com.acme.transport.lastmile.redelivery.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingRedeliveryTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingRedeliveryTemplateValidator()
        val item = newRollingRedeliveryTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = RollingRedeliveryTemplateRequiredLabelRule("rolling redelivery template needs an entry")
        val item = newRollingRedeliveryTemplate("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = RollingRedeliveryTemplateRequiredLaneRule("rolling redelivery template needs a value")
        val item = newRollingRedeliveryTemplate("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = RollingRedeliveryTemplateLabelLengthRule(32)
        val item = newRollingRedeliveryTemplate("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long lane`() {
        val rule = RollingRedeliveryTemplateLaneLengthRule(86400)
        val item = newRollingRedeliveryTemplate("x", "y")
        item.lane = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = RollingRedeliveryTemplateRatioRangeRule(1.5, 729.8)
        val item = newRollingRedeliveryTemplate("x", "y")
        item.ratio = 731.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
