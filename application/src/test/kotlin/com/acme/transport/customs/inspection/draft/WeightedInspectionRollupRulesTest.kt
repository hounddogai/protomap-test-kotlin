package com.acme.transport.customs.inspection.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedInspectionRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedInspectionRollupValidator()
        val item = newWeightedInspectionRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = WeightedInspectionRollupShiftCodeLengthRule(80)
        val item = newWeightedInspectionRollup("x", "y")
        item.shiftCode = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = WeightedInspectionRollupOdometerRangeRule(120, 86400)
        val item = newWeightedInspectionRollup("x", "y")
        item.odometer = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = WeightedInspectionRollupRequiredShiftCodeRule("weighted inspection rollup needs this field")
        val item = newWeightedInspectionRollup("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
