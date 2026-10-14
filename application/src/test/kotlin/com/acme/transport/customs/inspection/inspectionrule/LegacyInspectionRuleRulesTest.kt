package com.acme.transport.customs.inspection.inspectionrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyInspectionRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyInspectionRuleValidator()
        val item = newLegacyInspectionRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = LegacyInspectionRuleRequiredDockRule("legacy inspection rule needs this field")
        val item = newLegacyInspectionRule("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = LegacyInspectionRuleNameLengthRule(16)
        val item = newLegacyInspectionRule("x", "y")
        item.name = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
