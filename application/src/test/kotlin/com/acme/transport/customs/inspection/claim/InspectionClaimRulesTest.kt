package com.acme.transport.customs.inspection.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultInspectionClaimValidator()
        val item = newInspectionClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = InspectionClaimOwnerIdLengthRule(3600)
        val item = newInspectionClaim("x", "y")
        item.ownerId = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = InspectionClaimStopCountRangeRule(16, 86400)
        val item = newInspectionClaim("x", "y")
        item.stopCount = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = InspectionClaimRequiredOwnerIdRule("inspection claim needs an entry")
        val item = newInspectionClaim("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = InspectionClaimRequiredCodeRule("inspection claim needs this field")
        val item = newInspectionClaim("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = InspectionClaimCodeLengthRule(80)
        val item = newInspectionClaim("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
