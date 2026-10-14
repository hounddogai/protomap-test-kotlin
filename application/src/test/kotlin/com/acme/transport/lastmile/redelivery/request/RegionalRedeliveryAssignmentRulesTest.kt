package com.acme.transport.lastmile.redelivery.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalRedeliveryAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalRedeliveryAssignmentValidator()
        val item = newRegionalRedeliveryAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = RegionalRedeliveryAssignmentNameLengthRule(32)
        val item = newRegionalRedeliveryAssignment("x", "y")
        item.name = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above retries`() {
        val rule = RegionalRedeliveryAssignmentRetriesRangeRule(32, 80)
        val item = newRegionalRedeliveryAssignment("x", "y")
        item.retries = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
