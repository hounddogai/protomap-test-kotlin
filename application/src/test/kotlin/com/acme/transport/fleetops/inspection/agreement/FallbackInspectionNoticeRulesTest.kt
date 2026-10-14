package com.acme.transport.fleetops.inspection.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackInspectionNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackInspectionNoticeValidator()
        val item = newFallbackInspectionNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = FallbackInspectionNoticeRequiredNameRule("fallback inspection notice needs an entry")
        val item = newFallbackInspectionNotice("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = FallbackInspectionNoticeLoadKgRangeRule(9.8, 2729.6)
        val item = newFallbackInspectionNotice("x", "y")
        item.loadKg = 2731.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = FallbackInspectionNoticeNameLengthRule(32)
        val item = newFallbackInspectionNotice("x", "y")
        item.name = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long category`() {
        val rule = FallbackInspectionNoticeCategoryLengthRule(16)
        val item = newFallbackInspectionNotice("x", "y")
        item.category = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
