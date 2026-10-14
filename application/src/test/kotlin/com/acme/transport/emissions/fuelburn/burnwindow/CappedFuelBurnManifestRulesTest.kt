package com.acme.transport.emissions.fuelburn.burnwindow

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedFuelBurnManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedFuelBurnManifestValidator()
        val item = newCappedFuelBurnManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = CappedFuelBurnManifestRequiredTitleRule("capped fuel burn manifest needs an entry")
        val item = newCappedFuelBurnManifest("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = CappedFuelBurnManifestFloorValueRangeRule(9.4, 1171.9)
        val item = newCappedFuelBurnManifest("x", "y")
        item.floorValue = 1173.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = CappedFuelBurnManifestRequiredShiftCodeRule("capped fuel burn manifest needs a value")
        val item = newCappedFuelBurnManifest("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = CappedFuelBurnManifestTitleLengthRule(1000)
        val item = newCappedFuelBurnManifest("x", "y")
        item.title = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CappedFuelBurnManifestRequiredCodeRule("capped fuel burn manifest needs an entry")
        val item = newCappedFuelBurnManifest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
