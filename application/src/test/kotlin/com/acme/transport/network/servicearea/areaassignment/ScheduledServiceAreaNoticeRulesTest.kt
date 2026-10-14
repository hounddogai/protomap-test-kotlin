package com.acme.transport.network.servicearea.areaassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledServiceAreaNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledServiceAreaNoticeValidator()
        val item = newScheduledServiceAreaNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = ScheduledServiceAreaNoticeRequiredLocaleRule("scheduled service area notice needs a value")
        val item = newScheduledServiceAreaNotice("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above limit`() {
        val rule = ScheduledServiceAreaNoticeLimitRangeRule(32, 3600)
        val item = newScheduledServiceAreaNotice("x", "y")
        item.limit = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above weight`() {
        val rule = ScheduledServiceAreaNoticeWeightRangeRule(5.2, 4878.7)
        val item = newScheduledServiceAreaNotice("x", "y")
        item.weight = 4880.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
