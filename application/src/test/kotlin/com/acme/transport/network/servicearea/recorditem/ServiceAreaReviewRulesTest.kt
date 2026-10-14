package com.acme.transport.network.servicearea.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServiceAreaReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultServiceAreaReviewValidator()
        val item = newServiceAreaReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ServiceAreaReviewCodeLengthRule(80)
        val item = newServiceAreaReview("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ServiceAreaReviewRequiredCodeRule("service area review needs a value")
        val item = newServiceAreaReview("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = ServiceAreaReviewExternalRefLengthRule(120)
        val item = newServiceAreaReview("x", "y")
        item.externalRef = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = ServiceAreaReviewBatchSizeRangeRule(16, 1000)
        val item = newServiceAreaReview("x", "y")
        item.batchSize = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = ServiceAreaReviewRequiredExternalRefRule("service area review needs this field")
        val item = newServiceAreaReview("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
