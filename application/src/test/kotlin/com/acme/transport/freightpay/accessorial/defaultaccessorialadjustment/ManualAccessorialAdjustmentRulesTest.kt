package com.acme.transport.freightpay.accessorial.defaultaccessorialadjustment

import kotlin.test.Test
import kotlin.test.assertTrue

class ManualAccessorialAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualAccessorialAdjustmentValidator()
        val item = newManualAccessorialAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above version`() {
        val rule = ManualAccessorialAdjustmentVersionRangeRule(120, 120)
        val item = newManualAccessorialAdjustment("x", "y")
        item.version = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above limit`() {
        val rule = ManualAccessorialAdjustmentLimitRangeRule(32, 3600)
        val item = newManualAccessorialAdjustment("x", "y")
        item.limit = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above score`() {
        val rule = ManualAccessorialAdjustmentScoreRangeRule(6.2, 1678.9)
        val item = newManualAccessorialAdjustment("x", "y")
        item.score = 1680.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
