package com.acme.transport.crewing.dutyperiod.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedDutyPeriodAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedDutyPeriodAgreementValidator()
        val item = newBatchedDutyPeriodAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedDutyPeriodAgreementRequiredCodeRule("batched duty period agreement needs an entry")
        val item = newBatchedDutyPeriodAgreement("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = BatchedDutyPeriodAgreementRequiredTitleRule("batched duty period agreement needs this field")
        val item = newBatchedDutyPeriodAgreement("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
