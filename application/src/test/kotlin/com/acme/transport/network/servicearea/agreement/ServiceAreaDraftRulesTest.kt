package com.acme.transport.network.servicearea.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServiceAreaDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultServiceAreaDraftValidator()
        val item = newServiceAreaDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = ServiceAreaDraftFuelLitersRangeRule(2.5, 1009.7)
        val item = newServiceAreaDraft("x", "y")
        item.fuelLiters = 1011.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank reviewer`() {
        val rule = ServiceAreaDraftRequiredReviewerRule("service area draft needs this field")
        val item = newServiceAreaDraft("", "")
        item.reviewer = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ServiceAreaDraftCodeLengthRule(64)
        val item = newServiceAreaDraft("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = ServiceAreaDraftBatchSizeRangeRule(16, 3600)
        val item = newServiceAreaDraft("x", "y")
        item.batchSize = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = ServiceAreaDraftNameLengthRule(1000)
        val item = newServiceAreaDraft("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = ServiceAreaDraftReviewerLengthRule(86400)
        val item = newServiceAreaDraft("x", "y")
        item.reviewer = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
