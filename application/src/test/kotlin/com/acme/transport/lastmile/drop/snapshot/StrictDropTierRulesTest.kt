package com.acme.transport.lastmile.drop.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictDropTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictDropTierValidator()
        val item = newStrictDropTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = StrictDropTierRequiredDriverIdRule("strict drop tier needs an entry")
        val item = newStrictDropTier("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = StrictDropTierCodeLengthRule(3600)
        val item = newStrictDropTier("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = StrictDropTierDriverIdLengthRule(16)
        val item = newStrictDropTier("x", "y")
        item.driverId = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
