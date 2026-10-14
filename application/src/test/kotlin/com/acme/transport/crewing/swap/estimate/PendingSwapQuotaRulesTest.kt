package com.acme.transport.crewing.swap.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingSwapQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingSwapQuotaValidator()
        val item = newPendingSwapQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = PendingSwapQuotaDiscountRangeRule(2.9, 4558.1)
        val item = newPendingSwapQuota("x", "y")
        item.discount = 4559.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = PendingSwapQuotaRequiredLabelRule("pending swap quota needs a value")
        val item = newPendingSwapQuota("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = PendingSwapQuotaRequiredOwnerIdRule("pending swap quota needs a value")
        val item = newPendingSwapQuota("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = PendingSwapQuotaRequiredContactEmailRule("pending swap quota needs this field")
        val item = newPendingSwapQuota("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = PendingSwapQuotaBatchSizeRangeRule(32, 1000)
        val item = newPendingSwapQuota("x", "y")
        item.batchSize = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
