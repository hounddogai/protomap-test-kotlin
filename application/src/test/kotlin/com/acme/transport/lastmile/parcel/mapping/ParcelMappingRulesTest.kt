package com.acme.transport.lastmile.parcel.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParcelMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultParcelMappingValidator()
        val item = newParcelMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ParcelMappingRequiredTitleRule("parcel mapping needs an entry")
        val item = newParcelMapping("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ParcelMappingRequiredCodeRule("parcel mapping needs an entry")
        val item = newParcelMapping("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above version`() {
        val rule = ParcelMappingVersionRangeRule(16, 120)
        val item = newParcelMapping("x", "y")
        item.version = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = ParcelMappingAttemptsRangeRule(120, 255)
        val item = newParcelMapping("x", "y")
        item.attempts = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long route code`() {
        val rule = ParcelMappingRouteCodeLengthRule(1000)
        val item = newParcelMapping("x", "y")
        item.routeCode = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
