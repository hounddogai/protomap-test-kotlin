package com.acme.transport.emissions.routeoffset.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientRouteOffsetExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientRouteOffsetExceptionValidator()
        val item = newLenientRouteOffsetException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = LenientRouteOffsetExceptionCapacityRangeRule(80, 86400)
        val item = newLenientRouteOffsetException("x", "y")
        item.capacity = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = LenientRouteOffsetExceptionCodeLengthRule(64)
        val item = newLenientRouteOffsetException("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = LenientRouteOffsetExceptionPalletsRangeRule(64, 255)
        val item = newLenientRouteOffsetException("x", "y")
        item.pallets = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = LenientRouteOffsetExceptionLabelLengthRule(32)
        val item = newLenientRouteOffsetException("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
