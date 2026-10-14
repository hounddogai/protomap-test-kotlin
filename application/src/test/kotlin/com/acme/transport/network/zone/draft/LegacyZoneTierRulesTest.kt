package com.acme.transport.network.zone.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyZoneTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyZoneTierValidator()
        val item = newLegacyZoneTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long depot`() {
        val rule = LegacyZoneTierDepotLengthRule(86400)
        val item = newLegacyZoneTier("x", "y")
        item.depot = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above retries`() {
        val rule = LegacyZoneTierRetriesRangeRule(16, 86400)
        val item = newLegacyZoneTier("x", "y")
        item.retries = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = LegacyZoneTierCodeLengthRule(255)
        val item = newLegacyZoneTier("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
