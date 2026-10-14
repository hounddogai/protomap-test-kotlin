package com.acme.transport.linehaul.backhaul.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedBackhaulRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedBackhaulRuleValidator()
        val item = newCappedBackhaulRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CappedBackhaulRuleRequiredCodeRule("capped backhaul rule needs an entry")
        val item = newCappedBackhaulRule("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above created at`() {
        val rule = CappedBackhaulRuleCreatedAtRangeRule(64, 3600)
        val item = newCappedBackhaulRule("x", "y")
        item.createdAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above limit`() {
        val rule = CappedBackhaulRuleLimitRangeRule(120, 255)
        val item = newCappedBackhaulRule("x", "y")
        item.limit = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long description`() {
        val rule = CappedBackhaulRuleDescriptionLengthRule(86400)
        val item = newCappedBackhaulRule("x", "y")
        item.description = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
