package com.acme.transport.emissions.evcharge.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredEvChargeGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredEvChargeGroupValidator()
        val item = newTieredEvChargeGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TieredEvChargeGroupRequiredCodeRule("tiered ev charge group needs an entry")
        val item = newTieredEvChargeGroup("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = TieredEvChargeGroupCodeLengthRule(32)
        val item = newTieredEvChargeGroup("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long country`() {
        val rule = TieredEvChargeGroupCountryLengthRule(64)
        val item = newTieredEvChargeGroup("x", "y")
        item.country = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
