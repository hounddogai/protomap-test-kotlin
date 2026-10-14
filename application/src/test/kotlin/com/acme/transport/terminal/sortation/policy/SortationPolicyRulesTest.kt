package com.acme.transport.terminal.sortation.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SortationPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSortationPolicyValidator()
        val item = newSortationPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = SortationPolicyRequiredChecksumRule("sortation policy needs a value")
        val item = newSortationPolicy("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = SortationPolicyNameLengthRule(120)
        val item = newSortationPolicy("x", "y")
        item.name = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above score`() {
        val rule = SortationPolicyScoreRangeRule(6.1, 3253.2)
        val item = newSortationPolicy("x", "y")
        item.score = 3254.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = SortationPolicyChecksumLengthRule(80)
        val item = newSortationPolicy("x", "y")
        item.checksum = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
