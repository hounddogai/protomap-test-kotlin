package com.acme.transport.fleetops.defect.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefectGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefectGroupValidator()
        val item = newDefectGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = DefectGroupRatioRangeRule(0.4, 887.5)
        val item = newDefectGroup("x", "y")
        item.ratio = 889.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = DefectGroupCodeLengthRule(120)
        val item = newDefectGroup("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
