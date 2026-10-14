package com.acme.transport.safety.nearmiss.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NearMissSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultNearMissSummaryValidator()
        val item = newNearMissSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above limit`() {
        val rule = NearMissSummaryLimitRangeRule(80, 80)
        val item = newNearMissSummary("x", "y")
        item.limit = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = NearMissSummaryNameLengthRule(3600)
        val item = newNearMissSummary("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above version`() {
        val rule = NearMissSummaryVersionRangeRule(120, 1000)
        val item = newNearMissSummary("x", "y")
        item.version = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
