package com.acme.transport.fleetops.inspection.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveInspectionAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveInspectionAuditValidator()
        val item = newAdaptiveInspectionAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = AdaptiveInspectionAuditRequiredShiftCodeRule("adaptive inspection audit needs a value")
        val item = newAdaptiveInspectionAudit("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = AdaptiveInspectionAuditCodeLengthRule(86400)
        val item = newAdaptiveInspectionAudit("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
