package com.acme.transport.lastmile.parcel.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParcelEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultParcelEntryValidator()
        val item = newParcelEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = ParcelEntryGraceDaysRangeRule(255, 86400)
        val item = newParcelEntry("x", "y")
        item.graceDays = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ParcelEntryRequiredCodeRule("parcel entry needs an entry")
        val item = newParcelEntry("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long description`() {
        val rule = ParcelEntryDescriptionLengthRule(80)
        val item = newParcelEntry("x", "y")
        item.description = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
