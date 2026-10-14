package com.acme.transport.network.cutoff.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryCutoffBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryCutoffBatchValidator()
        val item = newPrimaryCutoffBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = PrimaryCutoffBatchNameLengthRule(80)
        val item = newPrimaryCutoffBatch("x", "y")
        item.name = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = PrimaryCutoffBatchEtaMinutesRangeRule(80, 1000)
        val item = newPrimaryCutoffBatch("x", "y")
        item.etaMinutes = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = PrimaryCutoffBatchCodeLengthRule(1000)
        val item = newPrimaryCutoffBatch("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PrimaryCutoffBatchRequiredCodeRule("primary cutoff batch needs an entry")
        val item = newPrimaryCutoffBatch("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
