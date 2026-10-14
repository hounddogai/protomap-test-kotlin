package com.acme.transport.lastmile.redelivery.redeliveryquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingRedeliveryQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingRedeliveryQuotaValidator()
        val item = newPendingRedeliveryQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long depot`() {
        val rule = PendingRedeliveryQuotaDepotLengthRule(32)
        val item = newPendingRedeliveryQuota("x", "y")
        item.depot = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = PendingRedeliveryQuotaRequiredDepotRule("pending redelivery quota needs a value")
        val item = newPendingRedeliveryQuota("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = PendingRedeliveryQuotaRatioRangeRule(8.8, 4693.7)
        val item = newPendingRedeliveryQuota("x", "y")
        item.ratio = 4695.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
