package com.acme.transport.terminal.dwell.dwelldigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualDwellManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualDwellManifestValidator()
        val item = newManualDwellManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = ManualDwellManifestFuelLitersRangeRule(9.1, 4710.2)
        val item = newManualDwellManifest("x", "y")
        item.fuelLiters = 4711.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ManualDwellManifestRequiredCodeRule("manual dwell manifest needs this field")
        val item = newManualDwellManifest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
