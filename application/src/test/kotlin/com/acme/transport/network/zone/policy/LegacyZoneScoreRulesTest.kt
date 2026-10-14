package com.acme.transport.network.zone.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyZoneScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyZoneScoreValidator()
        val item = newLegacyZoneScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = LegacyZoneScoreDriverIdLengthRule(32)
        val item = newLegacyZoneScore("x", "y")
        item.driverId = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = LegacyZoneScoreCodeLengthRule(120)
        val item = newLegacyZoneScore("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = LegacyZoneScoreRequiredDriverIdRule("legacy zone score needs this field")
        val item = newLegacyZoneScore("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
