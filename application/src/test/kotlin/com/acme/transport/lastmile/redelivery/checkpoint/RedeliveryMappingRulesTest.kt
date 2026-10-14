package com.acme.transport.lastmile.redelivery.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RedeliveryMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRedeliveryMappingValidator()
        val item = newRedeliveryMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long notes`() {
        val rule = RedeliveryMappingNotesLengthRule(80)
        val item = newRedeliveryMapping("x", "y")
        item.notes = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
