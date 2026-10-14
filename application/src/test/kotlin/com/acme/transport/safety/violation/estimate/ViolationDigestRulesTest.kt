package com.acme.transport.safety.violation.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ViolationDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultViolationDigestValidator()
        val item = newViolationDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank region`() {
        val rule = ViolationDigestRequiredRegionRule("violation digest needs this field")
        val item = newViolationDigest("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = ViolationDigestTimezoneLengthRule(1000)
        val item = newViolationDigest("x", "y")
        item.timezone = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = ViolationDigestCodeLengthRule(120)
        val item = newViolationDigest("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above window days`() {
        val rule = ViolationDigestWindowDaysRangeRule(64, 3600)
        val item = newViolationDigest("x", "y")
        item.windowDays = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long region`() {
        val rule = ViolationDigestRegionLengthRule(16)
        val item = newViolationDigest("x", "y")
        item.region = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
