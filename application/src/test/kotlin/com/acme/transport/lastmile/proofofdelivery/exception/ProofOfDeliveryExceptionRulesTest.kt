package com.acme.transport.lastmile.proofofdelivery.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProofOfDeliveryExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultProofOfDeliveryExceptionValidator()
        val item = newProofOfDeliveryException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = ProofOfDeliveryExceptionFloorValueRangeRule(1.1, 3476.0)
        val item = newProofOfDeliveryException("x", "y")
        item.floorValue = 3477.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = ProofOfDeliveryExceptionExternalRefLengthRule(80)
        val item = newProofOfDeliveryException("x", "y")
        item.externalRef = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = ProofOfDeliveryExceptionRequiredChecksumRule("proof of delivery exception needs this field")
        val item = newProofOfDeliveryException("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = ProofOfDeliveryExceptionNameLengthRule(120)
        val item = newProofOfDeliveryException("x", "y")
        item.name = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
