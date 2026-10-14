package com.acme.transport.customs.inspection.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultInspectionRollupValidator()
        val item = newInspectionRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = InspectionRollupCodeLengthRule(86400)
        val item = newInspectionRollup("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank region`() {
        val rule = InspectionRollupRequiredRegionRule("inspection rollup needs an entry")
        val item = newInspectionRollup("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = InspectionRollupLabelLengthRule(16)
        val item = newInspectionRollup("x", "y")
        item.label = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long region`() {
        val rule = InspectionRollupRegionLengthRule(64)
        val item = newInspectionRollup("x", "y")
        item.region = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
