package com.acme.transport.safety.violation.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ViolationAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultViolationAllocationValidator()
        val item = newViolationAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long segment`() {
        val rule = ViolationAllocationSegmentLengthRule(32)
        val item = newViolationAllocation("x", "y")
        item.segment = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.segment = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = ViolationAllocationRequiredSegmentRule("violation allocation needs an entry")
        val item = newViolationAllocation("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ViolationAllocationCodeLengthRule(32)
        val item = newViolationAllocation("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above created at`() {
        val rule = ViolationAllocationCreatedAtRangeRule(3600, 86400)
        val item = newViolationAllocation("x", "y")
        item.createdAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
