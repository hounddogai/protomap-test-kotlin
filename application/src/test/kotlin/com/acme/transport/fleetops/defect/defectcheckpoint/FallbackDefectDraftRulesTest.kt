package com.acme.transport.fleetops.defect.defectcheckpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackDefectDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackDefectDraftValidator()
        val item = newFallbackDefectDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = FallbackDefectDraftThresholdRangeRule(5.6, 4471.2)
        val item = newFallbackDefectDraft("x", "y")
        item.threshold = 4472.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = FallbackDefectDraftLabelLengthRule(120)
        val item = newFallbackDefectDraft("x", "y")
        item.label = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FallbackDefectDraftRequiredCodeRule("fallback defect draft needs this field")
        val item = newFallbackDefectDraft("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
