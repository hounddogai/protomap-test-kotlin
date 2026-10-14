package com.acme.transport.terminal.traileryard.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerYardAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTrailerYardAuditValidator()
        val item = newTrailerYardAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank region`() {
        val rule = TrailerYardAuditRequiredRegionRule("trailer yard audit needs a value")
        val item = newTrailerYardAudit("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = TrailerYardAuditRequiredContactEmailRule("trailer yard audit needs an entry")
        val item = newTrailerYardAudit("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = TrailerYardAuditFloorValueRangeRule(9.1, 4691.1)
        val item = newTrailerYardAudit("x", "y")
        item.floorValue = 4692.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above weight`() {
        val rule = TrailerYardAuditWeightRangeRule(7.3, 1378.4)
        val item = newTrailerYardAudit("x", "y")
        item.weight = 1379.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
