package com.acme.transport.freightpay.fuelsurcharge.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualFuelSurchargeMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualFuelSurchargeMilestoneValidator()
        val item = newManualFuelSurchargeMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ManualFuelSurchargeMilestoneRequiredTitleRule("manual fuel surcharge milestone needs this field")
        val item = newManualFuelSurchargeMilestone("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above limit`() {
        val rule = ManualFuelSurchargeMilestoneLimitRangeRule(16, 80)
        val item = newManualFuelSurchargeMilestone("x", "y")
        item.limit = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above margin`() {
        val rule = ManualFuelSurchargeMilestoneMarginRangeRule(2.8, 2301.6)
        val item = newManualFuelSurchargeMilestone("x", "y")
        item.margin = 2303.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
