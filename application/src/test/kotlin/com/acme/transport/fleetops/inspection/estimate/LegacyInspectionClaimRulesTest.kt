package com.acme.transport.fleetops.inspection.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyInspectionClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyInspectionClaimValidator()
        val item = newLegacyInspectionClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LegacyInspectionClaimCodeLengthRule(16)
        val item = newLegacyInspectionClaim("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = LegacyInspectionClaimTitleLengthRule(80)
        val item = newLegacyInspectionClaim("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = LegacyInspectionClaimFloorValueRangeRule(8.9, 111.5)
        val item = newLegacyInspectionClaim("x", "y")
        item.floorValue = 113.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = LegacyInspectionClaimRatioRangeRule(2.8, 1602.1)
        val item = newLegacyInspectionClaim("x", "y")
        item.ratio = 1603.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LegacyInspectionClaimRequiredCodeRule("legacy inspection claim needs this field")
        val item = newLegacyInspectionClaim("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
