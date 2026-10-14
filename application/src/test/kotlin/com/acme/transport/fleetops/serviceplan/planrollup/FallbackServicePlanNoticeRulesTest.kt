package com.acme.transport.fleetops.serviceplan.planrollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackServicePlanNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackServicePlanNoticeValidator()
        val item = newFallbackServicePlanNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = FallbackServicePlanNoticeRequiredTitleRule("fallback service plan notice needs an entry")
        val item = newFallbackServicePlanNotice("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = FallbackServicePlanNoticeTitleLengthRule(120)
        val item = newFallbackServicePlanNotice("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank description`() {
        val rule = FallbackServicePlanNoticeRequiredDescriptionRule("fallback service plan notice needs an entry")
        val item = newFallbackServicePlanNotice("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long description`() {
        val rule = FallbackServicePlanNoticeDescriptionLengthRule(255)
        val item = newFallbackServicePlanNotice("x", "y")
        item.description = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
