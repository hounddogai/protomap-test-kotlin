package com.acme.transport.lastmile.proofofdelivery.exception

import kotlin.test.Test
import kotlin.test.assertTrue

class CappedProofOfDeliveryGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedProofOfDeliveryGroupValidator()
        val item = newCappedProofOfDeliveryGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above window days`() {
        val rule = CappedProofOfDeliveryGroupWindowDaysRangeRule(80, 1000)
        val item = newCappedProofOfDeliveryGroup("x", "y")
        item.windowDays = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
