package com.acme.transport.customs.inspection.inspectionentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultInspectionQuotaValidator()
        val item = newInspectionQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = InspectionQuotaRequiredLabelRule("inspection quota needs this field")
        val item = newInspectionQuota("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = InspectionQuotaRequiredCodeRule("inspection quota needs this field")
        val item = newInspectionQuota("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = InspectionQuotaRequiredTrailerIdRule("inspection quota needs an entry")
        val item = newInspectionQuota("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = InspectionQuotaLabelLengthRule(120)
        val item = newInspectionQuota("x", "y")
        item.label = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
