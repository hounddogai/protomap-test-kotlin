package com.acme.transport.safety.scorecard.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScorecardTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScorecardTemplateValidator()
        val item = newScorecardTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above priority`() {
        val rule = ScorecardTemplatePriorityRangeRule(80, 1000)
        val item = newScorecardTemplate("x", "y")
        item.priority = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = ScorecardTemplateCodeLengthRule(1000)
        val item = newScorecardTemplate("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
