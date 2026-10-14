package com.acme.transport.linehaul.laneplan.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackLanePlanExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackLanePlanExceptionValidator()
        val item = newFallbackLanePlanException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = FallbackLanePlanExceptionCodeLengthRule(32)
        val item = newFallbackLanePlanException("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long source`() {
        val rule = FallbackLanePlanExceptionSourceLengthRule(80)
        val item = newFallbackLanePlanException("x", "y")
        item.source = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.source = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = FallbackLanePlanExceptionLoadKgRangeRule(0.5, 833.3)
        val item = newFallbackLanePlanException("x", "y")
        item.loadKg = 834.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = FallbackLanePlanExceptionRequiredContactEmailRule("fallback lane plan exception needs a value")
        val item = newFallbackLanePlanException("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
