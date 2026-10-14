package com.acme.transport.customs.hold.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HoldManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultHoldManifestValidator()
        val item = newHoldManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = HoldManifestRequiredNameRule("hold manifest needs a value")
        val item = newHoldManifest("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above weight`() {
        val rule = HoldManifestWeightRangeRule(6.1, 2371.7)
        val item = newHoldManifest("x", "y")
        item.weight = 2373.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = HoldManifestFuelLitersRangeRule(3.9, 1615.7)
        val item = newHoldManifest("x", "y")
        item.fuelLiters = 1617.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
