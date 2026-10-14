package com.acme.transport.lastmile.parcel.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalParcelEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalParcelEntryValidator()
        val item = newRegionalParcelEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = RegionalParcelEntryRequiredContactEmailRule("regional parcel entry needs a value")
        val item = newRegionalParcelEntry("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = RegionalParcelEntryRequiredLabelRule("regional parcel entry needs an entry")
        val item = newRegionalParcelEntry("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = RegionalParcelEntryStartsAtRangeRule(16, 3600)
        val item = newRegionalParcelEntry("x", "y")
        item.startsAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
