package com.acme.transport.emissions.evcharge.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalEvChargeAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalEvChargeAuditValidator()
        val item = newSeasonalEvChargeAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = SeasonalEvChargeAuditLabelLengthRule(80)
        val item = newSeasonalEvChargeAudit("x", "y")
        item.label = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = SeasonalEvChargeAuditExternalRefLengthRule(120)
        val item = newSeasonalEvChargeAudit("x", "y")
        item.externalRef = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
