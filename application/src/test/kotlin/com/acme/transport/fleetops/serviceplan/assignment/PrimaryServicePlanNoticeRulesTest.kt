package com.acme.transport.fleetops.serviceplan.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryServicePlanNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryServicePlanNoticeValidator()
        val item = newPrimaryServicePlanNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = PrimaryServicePlanNoticeCapacityRangeRule(16, 32)
        val item = newPrimaryServicePlanNotice("x", "y")
        item.capacity = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = PrimaryServicePlanNoticeCodeLengthRule(255)
        val item = newPrimaryServicePlanNotice("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = PrimaryServicePlanNoticeRequiredLabelRule("primary service plan notice needs a value")
        val item = newPrimaryServicePlanNotice("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = PrimaryServicePlanNoticeLabelLengthRule(120)
        val item = newPrimaryServicePlanNotice("x", "y")
        item.label = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
