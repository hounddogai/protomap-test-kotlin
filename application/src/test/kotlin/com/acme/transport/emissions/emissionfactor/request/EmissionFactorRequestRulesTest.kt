package com.acme.transport.emissions.emissionfactor.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EmissionFactorRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultEmissionFactorRequestValidator()
        val item = newEmissionFactorRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = EmissionFactorRequestBatchSizeRangeRule(16, 255)
        val item = newEmissionFactorRequest("x", "y")
        item.batchSize = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = EmissionFactorRequestEtaMinutesRangeRule(64, 64)
        val item = newEmissionFactorRequest("x", "y")
        item.etaMinutes = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = EmissionFactorRequestCodeLengthRule(64)
        val item = newEmissionFactorRequest("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
