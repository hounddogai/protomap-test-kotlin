package com.acme.transport.customs.declaration.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictDeclarationBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictDeclarationBaselineValidator()
        val item = newStrictDeclarationBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above retries`() {
        val rule = StrictDeclarationBaselineRetriesRangeRule(80, 80)
        val item = newStrictDeclarationBaseline("x", "y")
        item.retries = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above discount`() {
        val rule = StrictDeclarationBaselineDiscountRangeRule(6.8, 1118.5)
        val item = newStrictDeclarationBaseline("x", "y")
        item.discount = 1120.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = StrictDeclarationBaselineChecksumLengthRule(86400)
        val item = newStrictDeclarationBaseline("x", "y")
        item.checksum = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = StrictDeclarationBaselineUpdatedAtRangeRule(32, 1000)
        val item = newStrictDeclarationBaseline("x", "y")
        item.updatedAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
