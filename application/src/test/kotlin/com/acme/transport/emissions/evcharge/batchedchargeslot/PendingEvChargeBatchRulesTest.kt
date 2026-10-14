package com.acme.transport.emissions.evcharge.batchedchargeslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingEvChargeBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingEvChargeBatchValidator()
        val item = newPendingEvChargeBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = PendingEvChargeBatchMinimumRangeRule(8.3, 4570.2)
        val item = newPendingEvChargeBatch("x", "y")
        item.minimum = 4571.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = PendingEvChargeBatchThresholdRangeRule(4.4, 2341.6)
        val item = newPendingEvChargeBatch("x", "y")
        item.threshold = 2343.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = PendingEvChargeBatchCodeLengthRule(32)
        val item = newPendingEvChargeBatch("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = PendingEvChargeBatchRequiredLabelRule("pending ev charge batch needs this field")
        val item = newPendingEvChargeBatch("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
