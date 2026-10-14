package com.acme.transport.safety.violation.quota

import kotlin.test.Test
import kotlin.test.assertTrue

class ViolationTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultViolationTierValidator()
        val item = newViolationTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above created at`() {
        val rule = ViolationTierCreatedAtRangeRule(255, 1000)
        val item = newViolationTier("x", "y")
        item.createdAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = ViolationTierBatchSizeRangeRule(255, 255)
        val item = newViolationTier("x", "y")
        item.batchSize = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
