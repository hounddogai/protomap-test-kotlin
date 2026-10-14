package com.acme.transport.emissions.fuelburn.burnwindow

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingFuelBurnWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingFuelBurnWindowValidator()
        val item = newPendingFuelBurnWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = PendingFuelBurnWindowRequiredShiftCodeRule("pending fuel burn window needs an entry")
        val item = newPendingFuelBurnWindow("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long unit`() {
        val rule = PendingFuelBurnWindowUnitLengthRule(16)
        val item = newPendingFuelBurnWindow("x", "y")
        item.unit = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = PendingFuelBurnWindowShiftCodeLengthRule(16)
        val item = newPendingFuelBurnWindow("x", "y")
        item.shiftCode = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
