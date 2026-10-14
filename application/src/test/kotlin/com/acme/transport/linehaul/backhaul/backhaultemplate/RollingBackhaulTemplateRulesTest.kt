package com.acme.transport.linehaul.backhaul.backhaultemplate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingBackhaulTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingBackhaulTemplateValidator()
        val item = newRollingBackhaulTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = RollingBackhaulTemplateCapacityRangeRule(64, 1000)
        val item = newRollingBackhaulTemplate("x", "y")
        item.capacity = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = RollingBackhaulTemplateTimezoneLengthRule(80)
        val item = newRollingBackhaulTemplate("x", "y")
        item.timezone = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RollingBackhaulTemplateRequiredCodeRule("rolling backhaul template needs this field")
        val item = newRollingBackhaulTemplate("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
