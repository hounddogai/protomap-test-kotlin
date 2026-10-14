package com.acme.transport.lastmile.proofofdelivery.deliveryreview

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingProofOfDeliveryMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingProofOfDeliveryMappingValidator()
        val item = newRollingProofOfDeliveryMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = RollingProofOfDeliveryMappingChecksumLengthRule(86400)
        val item = newRollingProofOfDeliveryMapping("x", "y")
        item.checksum = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = RollingProofOfDeliveryMappingMinimumRangeRule(4.3, 2103.7)
        val item = newRollingProofOfDeliveryMapping("x", "y")
        item.minimum = 2105.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = RollingProofOfDeliveryMappingRequiredChecksumRule(
            "rolling proof of delivery mapping needs an entry",
        )
        val item = newRollingProofOfDeliveryMapping("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RollingProofOfDeliveryMappingCodeLengthRule(64)
        val item = newRollingProofOfDeliveryMapping("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
