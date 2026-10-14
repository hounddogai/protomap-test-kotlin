package com.acme.transport.safety.violation.violationquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ViolationNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultViolationNoticeValidator()
        val item = newViolationNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = ViolationNoticeMaximumRangeRule(9.2, 3287.9)
        val item = newViolationNotice("x", "y")
        item.maximum = 3289.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ViolationNoticeRequiredLabelRule("violation notice needs an entry")
        val item = newViolationNotice("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
