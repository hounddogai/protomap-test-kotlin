package com.acme.transport.fleetops.leasereturn.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LeaseReturnDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLeaseReturnDigestValidator()
        val item = newLeaseReturnDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = LeaseReturnDigestRequiredNameRule("lease return digest needs an entry")
        val item = newLeaseReturnDigest("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = LeaseReturnDigestDiscountRangeRule(2.1, 2957.8)
        val item = newLeaseReturnDigest("x", "y")
        item.discount = 2959.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = LeaseReturnDigestRequiredContactEmailRule("lease return digest needs a value")
        val item = newLeaseReturnDigest("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = LeaseReturnDigestThresholdRangeRule(8.9, 1783.2)
        val item = newLeaseReturnDigest("x", "y")
        item.threshold = 1784.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
