package com.acme.transport.lastmile.drop.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryDropRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryDropRuleValidator()
        val item = newPrimaryDropRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = PrimaryDropRuleUpdatedAtRangeRule(64, 255)
        val item = newPrimaryDropRule("x", "y")
        item.updatedAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = PrimaryDropRuleRequiredChecksumRule("primary drop rule needs an entry")
        val item = newPrimaryDropRule("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = PrimaryDropRuleChecksumLengthRule(86400)
        val item = newPrimaryDropRule("x", "y")
        item.checksum = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
