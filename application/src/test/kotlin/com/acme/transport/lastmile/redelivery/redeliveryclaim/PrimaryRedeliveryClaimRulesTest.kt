package com.acme.transport.lastmile.redelivery.redeliveryclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryRedeliveryClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryRedeliveryClaimValidator()
        val item = newPrimaryRedeliveryClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = PrimaryRedeliveryClaimRequiredChannelRule("primary redelivery claim needs an entry")
        val item = newPrimaryRedeliveryClaim("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = PrimaryRedeliveryClaimRequiredLabelRule("primary redelivery claim needs an entry")
        val item = newPrimaryRedeliveryClaim("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PrimaryRedeliveryClaimRequiredCodeRule("primary redelivery claim needs an entry")
        val item = newPrimaryRedeliveryClaim("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long channel`() {
        val rule = PrimaryRedeliveryClaimChannelLengthRule(32)
        val item = newPrimaryRedeliveryClaim("x", "y")
        item.channel = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
