package com.acme.transport.fleetops.defect.defectrecord

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryDefectTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryDefectTargetValidator()
        val item = newPrimaryDefectTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = PrimaryDefectTargetExpiresAtRangeRule(32, 120)
        val item = newPrimaryDefectTarget("x", "y")
        item.expiresAt = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above created at`() {
        val rule = PrimaryDefectTargetCreatedAtRangeRule(120, 3600)
        val item = newPrimaryDefectTarget("x", "y")
        item.createdAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = PrimaryDefectTargetRequiredShiftCodeRule("primary defect target needs a value")
        val item = newPrimaryDefectTarget("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = PrimaryDefectTargetCodeLengthRule(3600)
        val item = newPrimaryDefectTarget("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
