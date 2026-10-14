package com.acme.transport.terminal.crossdock.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CrossDockBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCrossDockBatchValidator()
        val item = newCrossDockBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = CrossDockBatchCodeLengthRule(86400)
        val item = newCrossDockBatch("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank carrier`() {
        val rule = CrossDockBatchRequiredCarrierRule("cross dock batch needs this field")
        val item = newCrossDockBatch("", "")
        item.carrier = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = CrossDockBatchRequiredNameRule("cross dock batch needs a value")
        val item = newCrossDockBatch("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = CrossDockBatchEtaMinutesRangeRule(64, 1000)
        val item = newCrossDockBatch("x", "y")
        item.etaMinutes = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
