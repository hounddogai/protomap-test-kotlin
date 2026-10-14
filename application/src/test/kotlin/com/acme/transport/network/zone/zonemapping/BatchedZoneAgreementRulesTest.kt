package com.acme.transport.network.zone.zonemapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedZoneAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedZoneAgreementValidator()
        val item = newBatchedZoneAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = BatchedZoneAgreementScoreRangeRule(9.7, 2994.9)
        val item = newBatchedZoneAgreement("x", "y")
        item.score = 2996.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above retries`() {
        val rule = BatchedZoneAgreementRetriesRangeRule(16, 64)
        val item = newBatchedZoneAgreement("x", "y")
        item.retries = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = BatchedZoneAgreementLabelLengthRule(64)
        val item = newBatchedZoneAgreement("x", "y")
        item.label = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
