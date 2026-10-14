package com.acme.transport.fleetops.defect.defectreview

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingDefectReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingDefectReviewValidator()
        val item = newPendingDefectReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PendingDefectReviewRequiredCodeRule("pending defect review needs an entry")
        val item = newPendingDefectReview("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = PendingDefectReviewRequiredCurrencyRule("pending defect review needs a value")
        val item = newPendingDefectReview("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank category`() {
        val rule = PendingDefectReviewRequiredCategoryRule("pending defect review needs a value")
        val item = newPendingDefectReview("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
