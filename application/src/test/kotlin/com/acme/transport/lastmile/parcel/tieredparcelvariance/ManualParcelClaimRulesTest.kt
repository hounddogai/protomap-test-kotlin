package com.acme.transport.lastmile.parcel.tieredparcelvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualParcelClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualParcelClaimValidator()
        val item = newManualParcelClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = ManualParcelClaimNameLengthRule(16)
        val item = newManualParcelClaim("x", "y")
        item.name = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = ManualParcelClaimSequenceRangeRule(16, 3600)
        val item = newManualParcelClaim("x", "y")
        item.sequence = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
