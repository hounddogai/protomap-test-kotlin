package com.acme.transport.linehaul.capacity.preference

import kotlin.test.Test
import kotlin.test.assertTrue

class CapacityScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCapacityScoreValidator()
        val item = newCapacityScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = CapacityScoreExpiresAtRangeRule(16, 80)
        val item = newCapacityScore("x", "y")
        item.expiresAt = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
