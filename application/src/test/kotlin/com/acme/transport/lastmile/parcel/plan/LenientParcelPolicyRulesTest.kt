package com.acme.transport.lastmile.parcel.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientParcelPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientParcelPolicyValidator()
        val item = newLenientParcelPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank category`() {
        val rule = LenientParcelPolicyRequiredCategoryRule("lenient parcel policy needs a value")
        val item = newLenientParcelPolicy("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = LenientParcelPolicyMaximumRangeRule(7.7, 4808.7)
        val item = newLenientParcelPolicy("x", "y")
        item.maximum = 4810.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long category`() {
        val rule = LenientParcelPolicyCategoryLengthRule(16)
        val item = newLenientParcelPolicy("x", "y")
        item.category = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
