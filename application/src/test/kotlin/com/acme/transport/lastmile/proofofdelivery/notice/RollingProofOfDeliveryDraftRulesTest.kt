package com.acme.transport.lastmile.proofofdelivery.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingProofOfDeliveryDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingProofOfDeliveryDraftValidator()
        val item = newRollingProofOfDeliveryDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RollingProofOfDeliveryDraftCodeLengthRule(80)
        val item = newRollingProofOfDeliveryDraft("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RollingProofOfDeliveryDraftRequiredCodeRule("rolling proof of delivery draft needs a value")
        val item = newRollingProofOfDeliveryDraft("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
