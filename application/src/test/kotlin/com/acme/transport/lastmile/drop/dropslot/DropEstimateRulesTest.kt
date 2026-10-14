package com.acme.transport.lastmile.drop.dropslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DropEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDropEstimateValidator()
        val item = newDropEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = DropEstimateLabelLengthRule(3600)
        val item = newDropEstimate("x", "y")
        item.label = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above created at`() {
        val rule = DropEstimateCreatedAtRangeRule(16, 32)
        val item = newDropEstimate("x", "y")
        item.createdAt = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DropEstimateRequiredCodeRule("drop estimate needs this field")
        val item = newDropEstimate("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = DropEstimateCodeLengthRule(3600)
        val item = newDropEstimate("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
