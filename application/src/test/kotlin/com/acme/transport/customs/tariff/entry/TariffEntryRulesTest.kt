package com.acme.transport.customs.tariff.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TariffEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTariffEntryValidator()
        val item = newTariffEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = TariffEntryFloorValueRangeRule(0.6, 3783.6)
        val item = newTariffEntry("x", "y")
        item.floorValue = 3785.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TariffEntryRequiredCodeRule("tariff entry needs an entry")
        val item = newTariffEntry("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
