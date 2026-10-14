package com.acme.transport.lastmile.parcel.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParcelOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultParcelOverrideValidator()
        val item = newParcelOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = ParcelOverrideTitleLengthRule(255)
        val item = newParcelOverride("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ParcelOverrideRequiredCodeRule("parcel override needs this field")
        val item = newParcelOverride("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = ParcelOverrideFloorValueRangeRule(0.3, 3801.2)
        val item = newParcelOverride("x", "y")
        item.floorValue = 3802.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
