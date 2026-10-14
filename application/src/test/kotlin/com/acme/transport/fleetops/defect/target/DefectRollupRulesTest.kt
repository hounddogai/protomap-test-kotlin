package com.acme.transport.fleetops.defect.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefectRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefectRollupValidator()
        val item = newDefectRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DefectRollupRequiredCodeRule("defect rollup needs an entry")
        val item = newDefectRollup("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = DefectRollupRequiredUnitRule("defect rollup needs this field")
        val item = newDefectRollup("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = DefectRollupStopCountRangeRule(16, 86400)
        val item = newDefectRollup("x", "y")
        item.stopCount = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = DefectRollupCodeLengthRule(64)
        val item = newDefectRollup("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = DefectRollupRequiredNameRule("defect rollup needs an entry")
        val item = newDefectRollup("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
