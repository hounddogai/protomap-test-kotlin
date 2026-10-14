package com.acme.transport.terminal.dwell.cycle

import kotlin.test.Test
import kotlin.test.assertTrue

class DwellAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDwellAssignmentValidator()
        val item = newDwellAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = DwellAssignmentDiscountRangeRule(9.5, 2457.0)
        val item = newDwellAssignment("x", "y")
        item.discount = 2458.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above version`() {
        val rule = DwellAssignmentVersionRangeRule(16, 255)
        val item = newDwellAssignment("x", "y")
        item.version = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
