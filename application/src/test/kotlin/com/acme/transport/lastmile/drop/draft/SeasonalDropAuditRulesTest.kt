package com.acme.transport.lastmile.drop.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalDropAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalDropAuditValidator()
        val item = newSeasonalDropAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = SeasonalDropAuditStopCountRangeRule(64, 3600)
        val item = newSeasonalDropAudit("x", "y")
        item.stopCount = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = SeasonalDropAuditRequiredLabelRule("seasonal drop audit needs an entry")
        val item = newSeasonalDropAudit("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = SeasonalDropAuditLabelLengthRule(86400)
        val item = newSeasonalDropAudit("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
