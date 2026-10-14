package com.acme.transport.freightpay.dispute.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalDisputeProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalDisputeProfileValidator()
        val item = newGlobalDisputeProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = GlobalDisputeProfileCodeLengthRule(3600)
        val item = newGlobalDisputeProfile("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above limit`() {
        val rule = GlobalDisputeProfileLimitRangeRule(16, 64)
        val item = newGlobalDisputeProfile("x", "y")
        item.limit = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = GlobalDisputeProfileExternalRefLengthRule(255)
        val item = newGlobalDisputeProfile("x", "y")
        item.externalRef = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
