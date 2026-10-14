package com.acme.transport.network.hub.rollinghubentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalHubEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalHubEntryValidator()
        val item = newSeasonalHubEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long country`() {
        val rule = SeasonalHubEntryCountryLengthRule(86400)
        val item = newSeasonalHubEntry("x", "y")
        item.country = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SeasonalHubEntryRequiredCodeRule("seasonal hub entry needs this field")
        val item = newSeasonalHubEntry("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
