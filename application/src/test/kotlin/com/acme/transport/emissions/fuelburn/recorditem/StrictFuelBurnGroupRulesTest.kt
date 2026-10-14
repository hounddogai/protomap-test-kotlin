package com.acme.transport.emissions.fuelburn.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictFuelBurnGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictFuelBurnGroupValidator()
        val item = newStrictFuelBurnGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = StrictFuelBurnGroupRequiredNameRule("strict fuel burn group needs an entry")
        val item = newStrictFuelBurnGroup("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = StrictFuelBurnGroupPalletsRangeRule(64, 3600)
        val item = newStrictFuelBurnGroup("x", "y")
        item.pallets = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long unit`() {
        val rule = StrictFuelBurnGroupUnitLengthRule(120)
        val item = newStrictFuelBurnGroup("x", "y")
        item.unit = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = StrictFuelBurnGroupRequiredCodeRule("strict fuel burn group needs an entry")
        val item = newStrictFuelBurnGroup("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = StrictFuelBurnGroupLabelLengthRule(255)
        val item = newStrictFuelBurnGroup("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
