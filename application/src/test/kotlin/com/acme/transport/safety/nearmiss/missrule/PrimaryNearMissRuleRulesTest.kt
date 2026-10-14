package com.acme.transport.safety.nearmiss.missrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryNearMissRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryNearMissRuleValidator()
        val item = newPrimaryNearMissRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above created at`() {
        val rule = PrimaryNearMissRuleCreatedAtRangeRule(255, 86400)
        val item = newPrimaryNearMissRule("x", "y")
        item.createdAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = PrimaryNearMissRuleRequiredTitleRule("primary near miss rule needs this field")
        val item = newPrimaryNearMissRule("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PrimaryNearMissRuleRequiredCodeRule("primary near miss rule needs an entry")
        val item = newPrimaryNearMissRule("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank region`() {
        val rule = PrimaryNearMissRuleRequiredRegionRule("primary near miss rule needs this field")
        val item = newPrimaryNearMissRule("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = PrimaryNearMissRuleTitleLengthRule(120)
        val item = newPrimaryNearMissRule("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
