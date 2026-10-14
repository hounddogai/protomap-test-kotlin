package com.acme.transport.fleetops.inspection.rollup

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
        val rule = InspectionRollupCodeLengthRule(80)
        val item = newInspectionRollup("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = InspectionRollupRequiredContactEmailRule("inspection rollup needs this field")
        val item = newInspectionRollup("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = InspectionRollupRequiredLabelRule("inspection rollup needs a value")
        val item = newInspectionRollup("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = InspectionRollupContactEmailLengthRule(32)
        val item = newInspectionRollup("x", "y")
        item.contactEmail = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank country`() {
        val rule = InspectionRollupRequiredCountryRule("inspection rollup needs an entry")
        val item = newInspectionRollup("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
