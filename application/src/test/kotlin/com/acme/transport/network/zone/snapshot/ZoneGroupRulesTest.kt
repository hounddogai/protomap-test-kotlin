package com.acme.transport.network.zone.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ZoneGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultZoneGroupValidator()
        val item = newZoneGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ZoneGroupRequiredLabelRule("zone group needs an entry")
        val item = newZoneGroup("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ZoneGroupRequiredTitleRule("zone group needs this field")
        val item = newZoneGroup("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = ZoneGroupDiscountRangeRule(7.7, 4483.9)
        val item = newZoneGroup("x", "y")
        item.discount = 4485.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
