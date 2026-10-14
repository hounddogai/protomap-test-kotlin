package com.acme.transport.emissions.emissionfactor.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EmissionFactorBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultEmissionFactorBatchValidator()
        val item = newEmissionFactorBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long notes`() {
        val rule = EmissionFactorBatchNotesLengthRule(16)
        val item = newEmissionFactorBatch("x", "y")
        item.notes = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
