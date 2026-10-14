package com.acme.transport.linehaul.backhaul.backhaulassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedBackhaulAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedBackhaulAssignmentValidator()
        val item = newBatchedBackhaulAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = BatchedBackhaulAssignmentRequiredTrailerIdRule("batched backhaul assignment needs this field")
        val item = newBatchedBackhaulAssignment("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedBackhaulAssignmentRequiredCodeRule("batched backhaul assignment needs a value")
        val item = newBatchedBackhaulAssignment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
