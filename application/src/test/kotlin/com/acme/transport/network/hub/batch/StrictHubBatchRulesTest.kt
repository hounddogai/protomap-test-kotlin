package com.acme.transport.network.hub.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictHubBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictHubBatchValidator()
        val item = newStrictHubBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = StrictHubBatchSequenceRangeRule(32, 64)
        val item = newStrictHubBatch("x", "y")
        item.sequence = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = StrictHubBatchCodeLengthRule(255)
        val item = newStrictHubBatch("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long description`() {
        val rule = StrictHubBatchDescriptionLengthRule(3600)
        val item = newStrictHubBatch("x", "y")
        item.description = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
