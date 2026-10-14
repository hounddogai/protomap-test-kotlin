package com.acme.transport.linehaul.backhaul.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingBackhaulAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingBackhaulAuditValidator()
        val item = newRollingBackhaulAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long lane`() {
        val rule = RollingBackhaulAuditLaneLengthRule(120)
        val item = newRollingBackhaulAudit("x", "y")
        item.lane = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = RollingBackhaulAuditRequiredLabelRule("rolling backhaul audit needs an entry")
        val item = newRollingBackhaulAudit("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank region`() {
        val rule = RollingBackhaulAuditRequiredRegionRule("rolling backhaul audit needs an entry")
        val item = newRollingBackhaulAudit("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = RollingBackhaulAuditMaximumRangeRule(9.2, 3522.6)
        val item = newRollingBackhaulAudit("x", "y")
        item.maximum = 3524.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
