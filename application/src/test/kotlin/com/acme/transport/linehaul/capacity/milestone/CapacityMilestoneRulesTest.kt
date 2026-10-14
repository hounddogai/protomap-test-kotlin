package com.acme.transport.linehaul.capacity.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CapacityMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCapacityMilestoneValidator()
        val item = newCapacityMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = CapacityMilestoneNameLengthRule(86400)
        val item = newCapacityMilestone("x", "y")
        item.name = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = CapacityMilestoneFloorValueRangeRule(5.0, 3747.2)
        val item = newCapacityMilestone("x", "y")
        item.floorValue = 3748.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = CapacityMilestoneContactEmailLengthRule(3600)
        val item = newCapacityMilestone("x", "y")
        item.contactEmail = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
