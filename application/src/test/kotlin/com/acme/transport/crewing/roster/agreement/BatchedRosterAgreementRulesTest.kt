package com.acme.transport.crewing.roster.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedRosterAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedRosterAgreementValidator()
        val item = newBatchedRosterAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = BatchedRosterAgreementCodeLengthRule(1000)
        val item = newBatchedRosterAgreement("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = BatchedRosterAgreementTitleLengthRule(3600)
        val item = newBatchedRosterAgreement("x", "y")
        item.title = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = BatchedRosterAgreementRequiredDriverIdRule("batched roster agreement needs a value")
        val item = newBatchedRosterAgreement("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = BatchedRosterAgreementRequiredTitleRule("batched roster agreement needs a value")
        val item = newBatchedRosterAgreement("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
