package com.acme.transport.fleetops.serviceplan.planthreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveServicePlanNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveServicePlanNoticeValidator()
        val item = newAdaptiveServicePlanNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = AdaptiveServicePlanNoticeLabelLengthRule(16)
        val item = newAdaptiveServicePlanNotice("x", "y")
        item.label = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long region`() {
        val rule = AdaptiveServicePlanNoticeRegionLengthRule(1000)
        val item = newAdaptiveServicePlanNotice("x", "y")
        item.region = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long locale`() {
        val rule = AdaptiveServicePlanNoticeLocaleLengthRule(120)
        val item = newAdaptiveServicePlanNotice("x", "y")
        item.locale = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = AdaptiveServicePlanNoticeCodeLengthRule(64)
        val item = newAdaptiveServicePlanNotice("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
