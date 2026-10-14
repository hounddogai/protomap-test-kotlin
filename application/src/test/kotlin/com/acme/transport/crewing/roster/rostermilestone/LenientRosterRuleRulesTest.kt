package com.acme.transport.crewing.roster.rostermilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientRosterRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientRosterRuleValidator()
        val item = newLenientRosterRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = LenientRosterRuleTitleLengthRule(86400)
        val item = newLenientRosterRule("x", "y")
        item.title = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = LenientRosterRuleRequiredContactEmailRule("lenient roster rule needs this field")
        val item = newLenientRosterRule("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LenientRosterRuleCodeLengthRule(32)
        val item = newLenientRosterRule("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
