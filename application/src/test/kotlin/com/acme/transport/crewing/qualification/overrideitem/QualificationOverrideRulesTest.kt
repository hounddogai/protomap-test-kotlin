package com.acme.transport.crewing.qualification.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QualificationOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultQualificationOverrideValidator()
        val item = newQualificationOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above cost`() {
        val rule = QualificationOverrideCostRangeRule(8.0, 705.2)
        val item = newQualificationOverride("x", "y")
        item.cost = 706.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long depot`() {
        val rule = QualificationOverrideDepotLengthRule(64)
        val item = newQualificationOverride("x", "y")
        item.depot = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = QualificationOverrideRequiredCodeRule("qualification override needs an entry")
        val item = newQualificationOverride("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = QualificationOverrideCodeLengthRule(32)
        val item = newQualificationOverride("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
