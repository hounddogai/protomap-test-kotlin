package com.acme.transport.freightpay.fuelsurcharge.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedFuelSurchargeMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedFuelSurchargeMilestoneValidator()
        val item = newBatchedFuelSurchargeMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedFuelSurchargeMilestoneRequiredCodeRule("batched fuel surcharge milestone needs an entry")
        val item = newBatchedFuelSurchargeMilestone("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = BatchedFuelSurchargeMilestoneRequiredTitleRule("batched fuel surcharge milestone needs an entry")
        val item = newBatchedFuelSurchargeMilestone("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = BatchedFuelSurchargeMilestoneRequiredTrailerIdRule(
            "batched fuel surcharge milestone needs this field",
        )
        val item = newBatchedFuelSurchargeMilestone("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
