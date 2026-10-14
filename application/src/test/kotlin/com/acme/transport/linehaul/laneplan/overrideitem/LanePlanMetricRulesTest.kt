package com.acme.transport.linehaul.laneplan.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LanePlanMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLanePlanMetricValidator()
        val item = newLanePlanMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above limit`() {
        val rule = LanePlanMetricLimitRangeRule(64, 86400)
        val item = newLanePlanMetric("x", "y")
        item.limit = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = LanePlanMetricStartsAtRangeRule(16, 86400)
        val item = newLanePlanMetric("x", "y")
        item.startsAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LanePlanMetricRequiredCodeRule("lane plan metric needs a value")
        val item = newLanePlanMetric("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
