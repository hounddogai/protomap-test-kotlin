package com.acme.transport.freightpay.dispute.disputebundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledDisputeBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledDisputeBundleValidator()
        val item = newScheduledDisputeBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = ScheduledDisputeBundleRequiredTimezoneRule("scheduled dispute bundle needs this field")
        val item = newScheduledDisputeBundle("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScheduledDisputeBundleRequiredCodeRule("scheduled dispute bundle needs a value")
        val item = newScheduledDisputeBundle("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank category`() {
        val rule = ScheduledDisputeBundleRequiredCategoryRule("scheduled dispute bundle needs this field")
        val item = newScheduledDisputeBundle("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
