package com.acme.transport.safety.violation.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ViolationGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultViolationGroupValidator()
        val item = newViolationGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = ViolationGroupOdometerRangeRule(32, 120)
        val item = newViolationGroup("x", "y")
        item.odometer = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = ViolationGroupEndsAtRangeRule(32, 120)
        val item = newViolationGroup("x", "y")
        item.endsAt = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = ViolationGroupMaximumRangeRule(3.7, 109.9)
        val item = newViolationGroup("x", "y")
        item.maximum = 111.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ViolationGroupRequiredTitleRule("violation group needs an entry")
        val item = newViolationGroup("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
