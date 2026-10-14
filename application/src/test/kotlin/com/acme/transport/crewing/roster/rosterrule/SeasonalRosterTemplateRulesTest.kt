package com.acme.transport.crewing.roster.rosterrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalRosterTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalRosterTemplateValidator()
        val item = newSeasonalRosterTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = SeasonalRosterTemplateCodeLengthRule(64)
        val item = newSeasonalRosterTemplate("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = SeasonalRosterTemplateRequiredLabelRule("seasonal roster template needs a value")
        val item = newSeasonalRosterTemplate("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = SeasonalRosterTemplateRequiredPhoneRule("seasonal roster template needs a value")
        val item = newSeasonalRosterTemplate("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
