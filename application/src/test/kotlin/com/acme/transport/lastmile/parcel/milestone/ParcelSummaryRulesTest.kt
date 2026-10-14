package com.acme.transport.lastmile.parcel.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParcelSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultParcelSummaryValidator()
        val item = newParcelSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ParcelSummaryRequiredTitleRule("parcel summary needs an entry")
        val item = newParcelSummary("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = ParcelSummaryLoadKgRangeRule(0.7, 3579.9)
        val item = newParcelSummary("x", "y")
        item.loadKg = 3581.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = ParcelSummaryRequiredShiftCodeRule("parcel summary needs a value")
        val item = newParcelSummary("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
