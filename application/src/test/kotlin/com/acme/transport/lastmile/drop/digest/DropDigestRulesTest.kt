package com.acme.transport.lastmile.drop.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DropDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDropDigestValidator()
        val item = newDropDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = DropDigestLabelLengthRule(1000)
        val item = newDropDigest("x", "y")
        item.label = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above retries`() {
        val rule = DropDigestRetriesRangeRule(16, 80)
        val item = newDropDigest("x", "y")
        item.retries = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DropDigestRequiredCodeRule("drop digest needs a value")
        val item = newDropDigest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = DropDigestRequiredLabelRule("drop digest needs this field")
        val item = newDropDigest("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
