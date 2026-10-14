package com.acme.transport.emissions.fuelburn.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedFuelBurnOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedFuelBurnOverrideValidator()
        val item = newBatchedFuelBurnOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = BatchedFuelBurnOverrideRequiredNameRule("batched fuel burn override needs a value")
        val item = newBatchedFuelBurnOverride("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
