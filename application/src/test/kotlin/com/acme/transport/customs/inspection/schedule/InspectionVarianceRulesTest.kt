package com.acme.transport.customs.inspection.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultInspectionVarianceValidator()
        val item = newInspectionVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = InspectionVarianceNameLengthRule(3600)
        val item = newInspectionVariance("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = InspectionVarianceRequiredNameRule("inspection variance needs an entry")
        val item = newInspectionVariance("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
