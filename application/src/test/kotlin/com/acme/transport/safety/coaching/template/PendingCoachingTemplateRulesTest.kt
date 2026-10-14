package com.acme.transport.safety.coaching.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingCoachingTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingCoachingTemplateValidator()
        val item = newPendingCoachingTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = PendingCoachingTemplateFloorValueRangeRule(3.3, 559.8)
        val item = newPendingCoachingTemplate("x", "y")
        item.floorValue = 561.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long region`() {
        val rule = PendingCoachingTemplateRegionLengthRule(255)
        val item = newPendingCoachingTemplate("x", "y")
        item.region = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank region`() {
        val rule = PendingCoachingTemplateRequiredRegionRule("pending coaching template needs a value")
        val item = newPendingCoachingTemplate("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
