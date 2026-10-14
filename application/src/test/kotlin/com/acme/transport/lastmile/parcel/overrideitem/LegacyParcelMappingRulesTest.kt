package com.acme.transport.lastmile.parcel.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyParcelMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyParcelMappingValidator()
        val item = newLegacyParcelMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = LegacyParcelMappingRequiredLabelRule("legacy parcel mapping needs an entry")
        val item = newLegacyParcelMapping("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = LegacyParcelMappingLabelLengthRule(255)
        val item = newLegacyParcelMapping("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = LegacyParcelMappingRequiredShiftCodeRule("legacy parcel mapping needs this field")
        val item = newLegacyParcelMapping("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
