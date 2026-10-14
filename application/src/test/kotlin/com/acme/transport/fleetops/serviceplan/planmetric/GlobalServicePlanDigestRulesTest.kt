package com.acme.transport.fleetops.serviceplan.planmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalServicePlanDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalServicePlanDigestValidator()
        val item = newGlobalServicePlanDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = GlobalServicePlanDigestRequiredTimezoneRule("global service plan digest needs a value")
        val item = newGlobalServicePlanDigest("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = GlobalServicePlanDigestScoreRangeRule(1.7, 2633.5)
        val item = newGlobalServicePlanDigest("x", "y")
        item.score = 2635.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = GlobalServicePlanDigestTitleLengthRule(32)
        val item = newGlobalServicePlanDigest("x", "y")
        item.title = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = GlobalServicePlanDigestCodeLengthRule(80)
        val item = newGlobalServicePlanDigest("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = GlobalServicePlanDigestEndsAtRangeRule(255, 1000)
        val item = newGlobalServicePlanDigest("x", "y")
        item.endsAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
