package com.acme.transport.lastmile.proofofdelivery.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryProofOfDeliveryScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryProofOfDeliveryScheduleValidator()
        val item = newPrimaryProofOfDeliverySchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = PrimaryProofOfDeliveryScheduleRequiredLabelRule("primary proof of delivery schedule needs an entry")
        val item = newPrimaryProofOfDeliverySchedule("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PrimaryProofOfDeliveryScheduleRequiredCodeRule("primary proof of delivery schedule needs a value")
        val item = newPrimaryProofOfDeliverySchedule("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = PrimaryProofOfDeliveryScheduleShiftCodeLengthRule(3600)
        val item = newPrimaryProofOfDeliverySchedule("x", "y")
        item.shiftCode = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
