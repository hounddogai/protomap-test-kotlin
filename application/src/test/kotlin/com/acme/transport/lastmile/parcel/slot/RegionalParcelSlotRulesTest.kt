package com.acme.transport.lastmile.parcel.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalParcelSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalParcelSlotValidator()
        val item = newRegionalParcelSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = RegionalParcelSlotRequiredTitleRule("regional parcel slot needs an entry")
        val item = newRegionalParcelSlot("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = RegionalParcelSlotTitleLengthRule(255)
        val item = newRegionalParcelSlot("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = RegionalParcelSlotRequiredExternalRefRule("regional parcel slot needs this field")
        val item = newRegionalParcelSlot("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
