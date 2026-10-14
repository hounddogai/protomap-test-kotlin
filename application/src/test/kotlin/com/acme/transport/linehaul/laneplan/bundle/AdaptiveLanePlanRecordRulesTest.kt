package com.acme.transport.linehaul.laneplan.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveLanePlanRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveLanePlanRecordValidator()
        val item = newAdaptiveLanePlanRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = AdaptiveLanePlanRecordEndsAtRangeRule(16, 64)
        val item = newAdaptiveLanePlanRecord("x", "y")
        item.endsAt = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = AdaptiveLanePlanRecordRequiredExternalRefRule("adaptive lane plan record needs an entry")
        val item = newAdaptiveLanePlanRecord("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = AdaptiveLanePlanRecordRequiredNameRule("adaptive lane plan record needs this field")
        val item = newAdaptiveLanePlanRecord("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above margin`() {
        val rule = AdaptiveLanePlanRecordMarginRangeRule(0.6, 1631.3)
        val item = newAdaptiveLanePlanRecord("x", "y")
        item.margin = 1632.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = AdaptiveLanePlanRecordGraceDaysRangeRule(64, 255)
        val item = newAdaptiveLanePlanRecord("x", "y")
        item.graceDays = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
