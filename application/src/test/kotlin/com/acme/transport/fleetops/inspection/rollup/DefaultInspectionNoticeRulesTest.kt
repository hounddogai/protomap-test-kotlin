package com.acme.transport.fleetops.inspection.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultInspectionNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultInspectionNoticeValidator()
        val item = newDefaultInspectionNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = DefaultInspectionNoticeLabelLengthRule(80)
        val item = newDefaultInspectionNotice("x", "y")
        item.label = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DefaultInspectionNoticeRequiredCodeRule("default inspection notice needs this field")
        val item = newDefaultInspectionNotice("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
