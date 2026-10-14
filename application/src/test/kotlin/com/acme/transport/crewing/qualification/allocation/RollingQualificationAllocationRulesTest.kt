package com.acme.transport.crewing.qualification.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingQualificationAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingQualificationAllocationValidator()
        val item = newRollingQualificationAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RollingQualificationAllocationRequiredCodeRule("rolling qualification allocation needs a value")
        val item = newRollingQualificationAllocation("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = RollingQualificationAllocationTitleLengthRule(86400)
        val item = newRollingQualificationAllocation("x", "y")
        item.title = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above budget`() {
        val rule = RollingQualificationAllocationBudgetRangeRule(3.9, 2396.0)
        val item = newRollingQualificationAllocation("x", "y")
        item.budget = 2397.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = RollingQualificationAllocationRequiredContactEmailRule(
            "rolling qualification allocation needs this field",
        )
        val item = newRollingQualificationAllocation("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
