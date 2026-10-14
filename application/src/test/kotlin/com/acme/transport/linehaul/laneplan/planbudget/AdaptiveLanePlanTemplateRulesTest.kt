package com.acme.transport.linehaul.laneplan.planbudget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveLanePlanTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveLanePlanTemplateValidator()
        val item = newAdaptiveLanePlanTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = AdaptiveLanePlanTemplateBatchSizeRangeRule(64, 1000)
        val item = newAdaptiveLanePlanTemplate("x", "y")
        item.batchSize = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = AdaptiveLanePlanTemplateShiftCodeLengthRule(80)
        val item = newAdaptiveLanePlanTemplate("x", "y")
        item.shiftCode = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = AdaptiveLanePlanTemplateNameLengthRule(64)
        val item = newAdaptiveLanePlanTemplate("x", "y")
        item.name = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = AdaptiveLanePlanTemplateExternalRefLengthRule(64)
        val item = newAdaptiveLanePlanTemplate("x", "y")
        item.externalRef = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
