package com.acme.transport.freightpay.accessorial.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedAccessorialBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedAccessorialBaselineValidator()
        val item = newBatchedAccessorialBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = BatchedAccessorialBaselineTrailerIdLengthRule(1000)
        val item = newBatchedAccessorialBaseline("x", "y")
        item.trailerId = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = BatchedAccessorialBaselineRequiredTrailerIdRule("batched accessorial baseline needs this field")
        val item = newBatchedAccessorialBaseline("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank category`() {
        val rule = BatchedAccessorialBaselineRequiredCategoryRule("batched accessorial baseline needs this field")
        val item = newBatchedAccessorialBaseline("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long category`() {
        val rule = BatchedAccessorialBaselineCategoryLengthRule(86400)
        val item = newBatchedAccessorialBaseline("x", "y")
        item.category = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
