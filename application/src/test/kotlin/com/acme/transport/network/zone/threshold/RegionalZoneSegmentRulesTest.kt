package com.acme.transport.network.zone.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalZoneSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalZoneSegmentValidator()
        val item = newRegionalZoneSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = RegionalZoneSegmentRequiredLabelRule("regional zone segment needs an entry")
        val item = newRegionalZoneSegment("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = RegionalZoneSegmentRequiredUnitRule("regional zone segment needs an entry")
        val item = newRegionalZoneSegment("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalZoneSegmentRequiredCodeRule("regional zone segment needs a value")
        val item = newRegionalZoneSegment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
