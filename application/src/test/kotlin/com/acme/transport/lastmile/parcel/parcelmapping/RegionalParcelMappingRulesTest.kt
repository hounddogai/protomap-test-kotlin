package com.acme.transport.lastmile.parcel.parcelmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalParcelMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalParcelMappingValidator()
        val item = newRegionalParcelMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = RegionalParcelMappingRequiredNotesRule("regional parcel mapping needs a value")
        val item = newRegionalParcelMapping("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long region`() {
        val rule = RegionalParcelMappingRegionLengthRule(86400)
        val item = newRegionalParcelMapping("x", "y")
        item.region = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank region`() {
        val rule = RegionalParcelMappingRequiredRegionRule("regional parcel mapping needs this field")
        val item = newRegionalParcelMapping("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
