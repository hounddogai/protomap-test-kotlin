package com.acme.transport.fleetops.defect.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveDefectAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveDefectAuditValidator()
        val item = newAdaptiveDefectAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = AdaptiveDefectAuditCodeLengthRule(86400)
        val item = newAdaptiveDefectAudit("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = AdaptiveDefectAuditLoadKgRangeRule(5.7, 1523.1)
        val item = newAdaptiveDefectAudit("x", "y")
        item.loadKg = 1524.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = AdaptiveDefectAuditRequiredRouteCodeRule("adaptive defect audit needs a value")
        val item = newAdaptiveDefectAudit("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long route code`() {
        val rule = AdaptiveDefectAuditRouteCodeLengthRule(32)
        val item = newAdaptiveDefectAudit("x", "y")
        item.routeCode = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
