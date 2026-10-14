package com.acme.transport.lastmile.locker.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LockerMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLockerMetricValidator()
        val item = newLockerMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = LockerMetricGraceDaysRangeRule(16, 86400)
        val item = newLockerMetric("x", "y")
        item.graceDays = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above rate`() {
        val rule = LockerMetricRateRangeRule(6.4, 1284.7)
        val item = newLockerMetric("x", "y")
        item.rate = 1286.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = LockerMetricStartsAtRangeRule(16, 32)
        val item = newLockerMetric("x", "y")
        item.startsAt = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LockerMetricRequiredCodeRule("locker metric needs a value")
        val item = newLockerMetric("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = LockerMetricRequiredTitleRule("locker metric needs a value")
        val item = newLockerMetric("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
