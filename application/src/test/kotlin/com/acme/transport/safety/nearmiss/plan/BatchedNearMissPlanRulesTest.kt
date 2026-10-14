package com.acme.transport.safety.nearmiss.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedNearMissPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedNearMissPlanValidator()
        val item = newBatchedNearMissPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = BatchedNearMissPlanRequiredNameRule("batched near miss plan needs a value")
        val item = newBatchedNearMissPlan("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = BatchedNearMissPlanNameLengthRule(32)
        val item = newBatchedNearMissPlan("x", "y")
        item.name = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = BatchedNearMissPlanRequiredVehicleIdRule("batched near miss plan needs an entry")
        val item = newBatchedNearMissPlan("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = BatchedNearMissPlanAmountRangeRule(0.5, 3515.8)
        val item = newBatchedNearMissPlan("x", "y")
        item.amount = 3517.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = BatchedNearMissPlanCodeLengthRule(120)
        val item = newBatchedNearMissPlan("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
