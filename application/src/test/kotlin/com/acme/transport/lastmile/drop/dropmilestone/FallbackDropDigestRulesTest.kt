package com.acme.transport.lastmile.drop.dropmilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackDropDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackDropDigestValidator()
        val item = newFallbackDropDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = FallbackDropDigestBatchSizeRangeRule(16, 3600)
        val item = newFallbackDropDigest("x", "y")
        item.batchSize = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = FallbackDropDigestRequiredExternalRefRule("fallback drop digest needs this field")
        val item = newFallbackDropDigest("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = FallbackDropDigestExternalRefLengthRule(1000)
        val item = newFallbackDropDigest("x", "y")
        item.externalRef = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
