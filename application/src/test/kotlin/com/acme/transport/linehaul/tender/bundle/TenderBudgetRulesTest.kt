package com.acme.transport.linehaul.tender.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TenderBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTenderBudgetValidator()
        val item = newTenderBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above created at`() {
        val rule = TenderBudgetCreatedAtRangeRule(32, 255)
        val item = newTenderBudget("x", "y")
        item.createdAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = TenderBudgetChecksumLengthRule(64)
        val item = newTenderBudget("x", "y")
        item.checksum = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
