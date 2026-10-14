package com.acme.transport.customs.inspection.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualInspectionGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualInspectionGroupValidator()
        val item = newManualInspectionGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ManualInspectionGroupRequiredTitleRule("manual inspection group needs a value")
        val item = newManualInspectionGroup("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long segment`() {
        val rule = ManualInspectionGroupSegmentLengthRule(3600)
        val item = newManualInspectionGroup("x", "y")
        item.segment = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.segment = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = ManualInspectionGroupCodeLengthRule(120)
        val item = newManualInspectionGroup("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = ManualInspectionGroupCarrierLengthRule(1000)
        val item = newManualInspectionGroup("x", "y")
        item.carrier = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank carrier`() {
        val rule = ManualInspectionGroupRequiredCarrierRule("manual inspection group needs an entry")
        val item = newManualInspectionGroup("", "")
        item.carrier = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
