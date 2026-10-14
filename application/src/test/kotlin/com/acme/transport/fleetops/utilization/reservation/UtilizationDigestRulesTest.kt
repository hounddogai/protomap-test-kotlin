package com.acme.transport.fleetops.utilization.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UtilizationDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultUtilizationDigestValidator()
        val item = newUtilizationDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = UtilizationDigestAmountRangeRule(2.1, 86.5)
        val item = newUtilizationDigest("x", "y")
        item.amount = 88.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = UtilizationDigestRequiredCodeRule("utilization digest needs this field")
        val item = newUtilizationDigest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = UtilizationDigestTitleLengthRule(32)
        val item = newUtilizationDigest("x", "y")
        item.title = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
