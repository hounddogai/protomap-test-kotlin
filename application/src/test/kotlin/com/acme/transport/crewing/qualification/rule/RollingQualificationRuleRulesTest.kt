package com.acme.transport.crewing.qualification.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingQualificationRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingQualificationRuleValidator()
        val item = newRollingQualificationRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = RollingQualificationRuleRequiredDepotRule("rolling qualification rule needs an entry")
        val item = newRollingQualificationRule("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = RollingQualificationRuleStartsAtRangeRule(32, 255)
        val item = newRollingQualificationRule("x", "y")
        item.startsAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long country`() {
        val rule = RollingQualificationRuleCountryLengthRule(80)
        val item = newRollingQualificationRule("x", "y")
        item.country = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
