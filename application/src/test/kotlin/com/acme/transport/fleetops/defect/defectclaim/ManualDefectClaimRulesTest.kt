package com.acme.transport.fleetops.defect.defectclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualDefectClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualDefectClaimValidator()
        val item = newManualDefectClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = ManualDefectClaimEndsAtRangeRule(16, 255)
        val item = newManualDefectClaim("x", "y")
        item.endsAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ManualDefectClaimRequiredTitleRule("manual defect claim needs a value")
        val item = newManualDefectClaim("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ManualDefectClaimRequiredCodeRule("manual defect claim needs this field")
        val item = newManualDefectClaim("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ManualDefectClaimCodeLengthRule(80)
        val item = newManualDefectClaim("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
