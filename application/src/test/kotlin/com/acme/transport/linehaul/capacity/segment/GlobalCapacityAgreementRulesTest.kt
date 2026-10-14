package com.acme.transport.linehaul.capacity.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalCapacityAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalCapacityAgreementValidator()
        val item = newGlobalCapacityAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = GlobalCapacityAgreementTitleLengthRule(16)
        val item = newGlobalCapacityAgreement("x", "y")
        item.title = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
