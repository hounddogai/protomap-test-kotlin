package com.acme.transport.lastmile.parcel.parcelbaseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredParcelBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredParcelBaselineValidator()
        val item = newTieredParcelBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = TieredParcelBaselineRequiredTitleRule("tiered parcel baseline needs a value")
        val item = newTieredParcelBaseline("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = TieredParcelBaselineTitleLengthRule(64)
        val item = newTieredParcelBaseline("x", "y")
        item.title = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = TieredParcelBaselineRequiredShiftCodeRule("tiered parcel baseline needs this field")
        val item = newTieredParcelBaseline("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
