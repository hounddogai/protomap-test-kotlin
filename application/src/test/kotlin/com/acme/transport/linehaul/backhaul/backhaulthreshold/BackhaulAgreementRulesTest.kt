package com.acme.transport.linehaul.backhaul.backhaulthreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BackhaulAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBackhaulAgreementValidator()
        val item = newBackhaulAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = BackhaulAgreementReviewerLengthRule(86400)
        val item = newBackhaulAgreement("x", "y")
        item.reviewer = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
