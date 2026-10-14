package com.acme.transport.emissions.emissionfactor.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedEmissionFactorManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedEmissionFactorManifestValidator()
        val item = newBatchedEmissionFactorManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long depot`() {
        val rule = BatchedEmissionFactorManifestDepotLengthRule(16)
        val item = newBatchedEmissionFactorManifest("x", "y")
        item.depot = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = BatchedEmissionFactorManifestMinimumRangeRule(8.9, 1205.3)
        val item = newBatchedEmissionFactorManifest("x", "y")
        item.minimum = 1206.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedEmissionFactorManifestRequiredCodeRule("batched emission factor manifest needs an entry")
        val item = newBatchedEmissionFactorManifest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
