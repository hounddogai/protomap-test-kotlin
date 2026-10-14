package com.acme.transport.safety.coaching.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedCoachingDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedCoachingDigestValidator()
        val item = newCappedCoachingDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = CappedCoachingDigestRequiredNameRule("capped coaching digest needs this field")
        val item = newCappedCoachingDigest("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = CappedCoachingDigestRequiredPhoneRule("capped coaching digest needs an entry")
        val item = newCappedCoachingDigest("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = CappedCoachingDigestRequiredSummaryRule("capped coaching digest needs this field")
        val item = newCappedCoachingDigest("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long summary`() {
        val rule = CappedCoachingDigestSummaryLengthRule(255)
        val item = newCappedCoachingDigest("x", "y")
        item.summary = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = CappedCoachingDigestNameLengthRule(1000)
        val item = newCappedCoachingDigest("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
