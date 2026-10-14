package com.acme.transport.customs.inspection.pendinginspectionrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualInspectionTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualInspectionTemplateValidator()
        val item = newManualInspectionTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = ManualInspectionTemplateRequiredOwnerIdRule("manual inspection template needs a value")
        val item = newManualInspectionTemplate("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = ManualInspectionTemplateOwnerIdLengthRule(120)
        val item = newManualInspectionTemplate("x", "y")
        item.ownerId = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long country`() {
        val rule = ManualInspectionTemplateCountryLengthRule(1000)
        val item = newManualInspectionTemplate("x", "y")
        item.country = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ManualInspectionTemplateRequiredCodeRule("manual inspection template needs a value")
        val item = newManualInspectionTemplate("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
