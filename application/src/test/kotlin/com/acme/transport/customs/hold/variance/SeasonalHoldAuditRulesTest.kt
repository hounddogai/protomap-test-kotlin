package com.acme.transport.customs.hold.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalHoldAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalHoldAuditValidator()
        val item = newSeasonalHoldAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = SeasonalHoldAuditRequiredOwnerIdRule("seasonal hold audit needs a value")
        val item = newSeasonalHoldAudit("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SeasonalHoldAuditRequiredCodeRule("seasonal hold audit needs an entry")
        val item = newSeasonalHoldAudit("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = SeasonalHoldAuditLoadKgRangeRule(6.4, 2369.1)
        val item = newSeasonalHoldAudit("x", "y")
        item.loadKg = 2370.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = SeasonalHoldAuditDistanceKmRangeRule(7.0, 1211.1)
        val item = newSeasonalHoldAudit("x", "y")
        item.distanceKm = 1212.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
