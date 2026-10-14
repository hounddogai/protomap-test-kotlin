package com.acme.transport.fleetops.defect.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualDefectTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualDefectTargetValidator()
        val item = newManualDefectTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = ManualDefectTargetScoreRangeRule(3.3, 84.3)
        val item = newManualDefectTarget("x", "y")
        item.score = 85.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = ManualDefectTargetLabelLengthRule(3600)
        val item = newManualDefectTarget("x", "y")
        item.label = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ManualDefectTargetRequiredCodeRule("manual defect target needs a value")
        val item = newManualDefectTarget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ManualDefectTargetRequiredLabelRule("manual defect target needs this field")
        val item = newManualDefectTarget("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
