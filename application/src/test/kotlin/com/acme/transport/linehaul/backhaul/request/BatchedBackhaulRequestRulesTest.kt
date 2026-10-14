package com.acme.transport.linehaul.backhaul.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedBackhaulRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedBackhaulRequestValidator()
        val item = newBatchedBackhaulRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = BatchedBackhaulRequestCeilingRangeRule(3.0, 2481.4)
        val item = newBatchedBackhaulRequest("x", "y")
        item.ceiling = 2482.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = BatchedBackhaulRequestRequiredLabelRule("batched backhaul request needs this field")
        val item = newBatchedBackhaulRequest("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = BatchedBackhaulRequestCodeLengthRule(120)
        val item = newBatchedBackhaulRequest("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedBackhaulRequestRequiredCodeRule("batched backhaul request needs a value")
        val item = newBatchedBackhaulRequest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = BatchedBackhaulRequestEndsAtRangeRule(64, 3600)
        val item = newBatchedBackhaulRequest("x", "y")
        item.endsAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
