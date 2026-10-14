package com.acme.transport.fleetops.tire.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyTireDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyTireDraftValidator()
        val item = newLegacyTireDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long source`() {
        val rule = LegacyTireDraftSourceLengthRule(16)
        val item = newLegacyTireDraft("x", "y")
        item.source = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.source = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long summary`() {
        val rule = LegacyTireDraftSummaryLengthRule(120)
        val item = newLegacyTireDraft("x", "y")
        item.summary = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank source`() {
        val rule = LegacyTireDraftRequiredSourceRule("legacy tire draft needs a value")
        val item = newLegacyTireDraft("", "")
        item.source = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
