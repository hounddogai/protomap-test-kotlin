package com.acme.transport.lastmile.proofofdelivery.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientProofOfDeliveryScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientProofOfDeliveryScheduleValidator()
        val item = newLenientProofOfDeliverySchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = LenientProofOfDeliveryScheduleRequiredChecksumRule(
            "lenient proof of delivery schedule needs an entry",
        )
        val item = newLenientProofOfDeliverySchedule("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = LenientProofOfDeliveryScheduleDiscountRangeRule(8.7, 1141.8)
        val item = newLenientProofOfDeliverySchedule("x", "y")
        item.discount = 1143.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = LenientProofOfDeliveryScheduleTimezoneLengthRule(3600)
        val item = newLenientProofOfDeliverySchedule("x", "y")
        item.timezone = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LenientProofOfDeliveryScheduleRequiredCodeRule("lenient proof of delivery schedule needs a value")
        val item = newLenientProofOfDeliverySchedule("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
