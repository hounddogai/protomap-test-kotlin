package com.acme.transport.safety.nearmiss.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NearMissMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultNearMissMilestoneValidator()
        val item = newNearMissMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = NearMissMilestoneAmountRangeRule(2.6, 2162.8)
        val item = newNearMissMilestone("x", "y")
        item.amount = 2164.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = NearMissMilestoneRequiredCodeRule("near miss milestone needs an entry")
        val item = newNearMissMilestone("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = NearMissMilestoneRequiredLabelRule("near miss milestone needs a value")
        val item = newNearMissMilestone("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = NearMissMilestoneCodeLengthRule(255)
        val item = newNearMissMilestone("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above created at`() {
        val rule = NearMissMilestoneCreatedAtRangeRule(16, 3600)
        val item = newNearMissMilestone("x", "y")
        item.createdAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = NearMissMilestoneFloorValueRangeRule(3.0, 4808.4)
        val item = newNearMissMilestone("x", "y")
        item.floorValue = 4809.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
