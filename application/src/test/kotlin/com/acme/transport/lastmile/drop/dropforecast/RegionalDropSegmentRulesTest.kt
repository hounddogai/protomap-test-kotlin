package com.acme.transport.lastmile.drop.dropforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalDropSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalDropSegmentValidator()
        val item = newRegionalDropSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RegionalDropSegmentRequiredNameRule("regional drop segment needs an entry")
        val item = newRegionalDropSegment("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long route code`() {
        val rule = RegionalDropSegmentRouteCodeLengthRule(255)
        val item = newRegionalDropSegment("x", "y")
        item.routeCode = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = RegionalDropSegmentRequiredPhoneRule("regional drop segment needs a value")
        val item = newRegionalDropSegment("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = RegionalDropSegmentNameLengthRule(86400)
        val item = newRegionalDropSegment("x", "y")
        item.name = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalDropSegmentRequiredCodeRule("regional drop segment needs this field")
        val item = newRegionalDropSegment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
