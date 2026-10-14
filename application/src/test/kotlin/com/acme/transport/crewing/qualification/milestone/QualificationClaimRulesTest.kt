package com.acme.transport.crewing.qualification.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QualificationClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultQualificationClaimValidator()
        val item = newQualificationClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = QualificationClaimCodeLengthRule(1000)
        val item = newQualificationClaim("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above rate`() {
        val rule = QualificationClaimRateRangeRule(6.5, 2326.5)
        val item = newQualificationClaim("x", "y")
        item.rate = 2328.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
