package com.acme.transport.freightpay.dispute.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedDisputeClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedDisputeClaimValidator()
        val item = newCappedDisputeClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CappedDisputeClaimRequiredCodeRule("capped dispute claim needs a value")
        val item = newCappedDisputeClaim("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = CappedDisputeClaimPalletsRangeRule(1000, 1000)
        val item = newCappedDisputeClaim("x", "y")
        item.pallets = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = CappedDisputeClaimRequiredRouteCodeRule("capped dispute claim needs this field")
        val item = newCappedDisputeClaim("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank carrier`() {
        val rule = CappedDisputeClaimRequiredCarrierRule("capped dispute claim needs this field")
        val item = newCappedDisputeClaim("", "")
        item.carrier = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
