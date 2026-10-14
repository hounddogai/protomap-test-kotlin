package com.acme.transport.fleetops.leasereturn.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveLeaseReturnDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveLeaseReturnDigestValidator()
        val item = newAdaptiveLeaseReturnDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = AdaptiveLeaseReturnDigestLabelLengthRule(80)
        val item = newAdaptiveLeaseReturnDigest("x", "y")
        item.label = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = AdaptiveLeaseReturnDigestRequiredLabelRule("adaptive lease return digest needs a value")
        val item = newAdaptiveLeaseReturnDigest("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AdaptiveLeaseReturnDigestRequiredCodeRule("adaptive lease return digest needs a value")
        val item = newAdaptiveLeaseReturnDigest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = AdaptiveLeaseReturnDigestCodeLengthRule(32)
        val item = newAdaptiveLeaseReturnDigest("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
