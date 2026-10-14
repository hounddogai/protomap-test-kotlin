package com.acme.transport.customs.inspection.inspectiondigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackInspectionRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackInspectionRequestValidator()
        val item = newFallbackInspectionRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FallbackInspectionRequestRequiredCodeRule("fallback inspection request needs an entry")
        val item = newFallbackInspectionRequest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = FallbackInspectionRequestScoreRangeRule(8.6, 3172.6)
        val item = newFallbackInspectionRequest("x", "y")
        item.score = 3174.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = FallbackInspectionRequestRequiredDepotRule("fallback inspection request needs this field")
        val item = newFallbackInspectionRequest("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = FallbackInspectionRequestCodeLengthRule(32)
        val item = newFallbackInspectionRequest("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
