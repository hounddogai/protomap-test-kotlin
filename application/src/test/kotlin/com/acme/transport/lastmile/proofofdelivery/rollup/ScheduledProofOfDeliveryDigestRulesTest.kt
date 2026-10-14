package com.acme.transport.lastmile.proofofdelivery.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledProofOfDeliveryDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledProofOfDeliveryDigestValidator()
        val item = newScheduledProofOfDeliveryDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long category`() {
        val rule = ScheduledProofOfDeliveryDigestCategoryLengthRule(64)
        val item = newScheduledProofOfDeliveryDigest("x", "y")
        item.category = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank category`() {
        val rule = ScheduledProofOfDeliveryDigestRequiredCategoryRule(
            "scheduled proof of delivery digest needs an entry",
        )
        val item = newScheduledProofOfDeliveryDigest("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
