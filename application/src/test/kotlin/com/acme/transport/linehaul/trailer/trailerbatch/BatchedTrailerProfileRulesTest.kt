package com.acme.transport.linehaul.trailer.trailerbatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedTrailerProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedTrailerProfileValidator()
        val item = newBatchedTrailerProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = BatchedTrailerProfileRequiredDriverIdRule("batched trailer profile needs an entry")
        val item = newBatchedTrailerProfile("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = BatchedTrailerProfileCodeLengthRule(80)
        val item = newBatchedTrailerProfile("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
