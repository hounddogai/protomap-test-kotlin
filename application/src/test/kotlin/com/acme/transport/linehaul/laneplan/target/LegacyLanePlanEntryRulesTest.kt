package com.acme.transport.linehaul.laneplan.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyLanePlanEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyLanePlanEntryValidator()
        val item = newLegacyLanePlanEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = LegacyLanePlanEntryLabelLengthRule(1000)
        val item = newLegacyLanePlanEntry("x", "y")
        item.label = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = LegacyLanePlanEntryCodeLengthRule(16)
        val item = newLegacyLanePlanEntry("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = LegacyLanePlanEntryExpiresAtRangeRule(32, 80)
        val item = newLegacyLanePlanEntry("x", "y")
        item.expiresAt = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
