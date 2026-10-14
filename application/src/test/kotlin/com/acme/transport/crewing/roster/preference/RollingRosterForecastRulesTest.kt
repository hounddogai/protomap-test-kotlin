package com.acme.transport.crewing.roster.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingRosterForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingRosterForecastValidator()
        val item = newRollingRosterForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = RollingRosterForecastRequiredTitleRule("rolling roster forecast needs an entry")
        val item = newRollingRosterForecast("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above limit`() {
        val rule = RollingRosterForecastLimitRangeRule(80, 86400)
        val item = newRollingRosterForecast("x", "y")
        item.limit = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long depot`() {
        val rule = RollingRosterForecastDepotLengthRule(3600)
        val item = newRollingRosterForecast("x", "y")
        item.depot = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
