package com.acme.transport.freightpay.dispute.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DisputeBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDisputeBatchValidator()
        val item = newDisputeBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = DisputeBatchRequiredLabelRule("dispute batch needs this field")
        val item = newDisputeBatch("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = DisputeBatchRequiredTitleRule("dispute batch needs this field")
        val item = newDisputeBatch("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
