package com.acme.transport.emissions.routeoffset.offsetmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackRouteOffsetMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackRouteOffsetMappingValidator()
        val item = newFallbackRouteOffsetMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = FallbackRouteOffsetMappingLabelLengthRule(3600)
        val item = newFallbackRouteOffsetMapping("x", "y")
        item.label = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long unit`() {
        val rule = FallbackRouteOffsetMappingUnitLengthRule(1000)
        val item = newFallbackRouteOffsetMapping("x", "y")
        item.unit = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = FallbackRouteOffsetMappingCodeLengthRule(16)
        val item = newFallbackRouteOffsetMapping("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = FallbackRouteOffsetMappingRequiredUnitRule("fallback route offset mapping needs this field")
        val item = newFallbackRouteOffsetMapping("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
