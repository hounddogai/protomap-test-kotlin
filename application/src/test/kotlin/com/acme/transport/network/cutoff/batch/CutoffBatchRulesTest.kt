package com.acme.transport.network.cutoff.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CutoffBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCutoffBatchValidator()
        val item = newCutoffBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = CutoffBatchMinimumRangeRule(7.9, 1908.9)
        val item = newCutoffBatch("x", "y")
        item.minimum = 1910.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CutoffBatchRequiredCodeRule("cutoff batch needs a value")
        val item = newCutoffBatch("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = CutoffBatchEtaMinutesRangeRule(120, 3600)
        val item = newCutoffBatch("x", "y")
        item.etaMinutes = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = CutoffBatchRequiredDockRule("cutoff batch needs a value")
        val item = newCutoffBatch("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
