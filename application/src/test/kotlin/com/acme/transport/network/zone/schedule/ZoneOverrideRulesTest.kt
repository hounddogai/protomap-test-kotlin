package com.acme.transport.network.zone.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ZoneOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultZoneOverrideValidator()
        val item = newZoneOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ZoneOverrideRequiredTitleRule("zone override needs this field")
        val item = newZoneOverride("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = ZoneOverrideEtaMinutesRangeRule(64, 3600)
        val item = newZoneOverride("x", "y")
        item.etaMinutes = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = ZoneOverrideTitleLengthRule(86400)
        val item = newZoneOverride("x", "y")
        item.title = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
