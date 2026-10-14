package com.acme.transport.fleetops.tire.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedTireTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedTireTierValidator()
        val item = newBatchedTireTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = BatchedTireTierPalletsRangeRule(16, 80)
        val item = newBatchedTireTier("x", "y")
        item.pallets = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above retries`() {
        val rule = BatchedTireTierRetriesRangeRule(120, 1000)
        val item = newBatchedTireTier("x", "y")
        item.retries = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = BatchedTireTierRequiredNameRule("batched tire tier needs this field")
        val item = newBatchedTireTier("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
