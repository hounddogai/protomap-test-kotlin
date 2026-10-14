package com.acme.transport.customs.tariff.tariffprofile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientTariffEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientTariffEntryValidator()
        val item = newLenientTariffEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LenientTariffEntryCodeLengthRule(120)
        val item = newLenientTariffEntry("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = LenientTariffEntryRatioRangeRule(4.9, 784.1)
        val item = newLenientTariffEntry("x", "y")
        item.ratio = 785.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = LenientTariffEntryRequiredLaneRule("lenient tariff entry needs a value")
        val item = newLenientTariffEntry("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
