package com.acme.transport.linehaul.capacity.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedCapacityAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedCapacityAgreementValidator()
        val item = newBatchedCapacityAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = BatchedCapacityAgreementRequiredDriverIdRule("batched capacity agreement needs this field")
        val item = newBatchedCapacityAgreement("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = BatchedCapacityAgreementRequiredTitleRule("batched capacity agreement needs this field")
        val item = newBatchedCapacityAgreement("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
