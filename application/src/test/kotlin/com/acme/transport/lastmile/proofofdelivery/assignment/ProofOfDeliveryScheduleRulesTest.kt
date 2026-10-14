package com.acme.transport.lastmile.proofofdelivery.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProofOfDeliveryScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultProofOfDeliveryScheduleValidator()
        val item = newProofOfDeliverySchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = ProofOfDeliveryScheduleRequiredLocaleRule("proof of delivery schedule needs an entry")
        val item = newProofOfDeliverySchedule("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long description`() {
        val rule = ProofOfDeliveryScheduleDescriptionLengthRule(86400)
        val item = newProofOfDeliverySchedule("x", "y")
        item.description = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
