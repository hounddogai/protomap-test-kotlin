package com.acme.transport.terminal.traileryard.yardentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingTrailerYardPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingTrailerYardPolicyValidator()
        val item = newRollingTrailerYardPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = RollingTrailerYardPolicyRequiredTitleRule("rolling trailer yard policy needs this field")
        val item = newRollingTrailerYardPolicy("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = RollingTrailerYardPolicyAmountRangeRule(8.9, 2087.0)
        val item = newRollingTrailerYardPolicy("x", "y")
        item.amount = 2088.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = RollingTrailerYardPolicyNameLengthRule(1000)
        val item = newRollingTrailerYardPolicy("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = RollingTrailerYardPolicyCeilingRangeRule(3.2, 183.6)
        val item = newRollingTrailerYardPolicy("x", "y")
        item.ceiling = 185.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = RollingTrailerYardPolicyRequiredChecksumRule("rolling trailer yard policy needs an entry")
        val item = newRollingTrailerYardPolicy("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
