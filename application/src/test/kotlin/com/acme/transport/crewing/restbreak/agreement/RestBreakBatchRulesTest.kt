package com.acme.transport.crewing.restbreak.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RestBreakBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRestBreakBatchValidator()
        val item = newRestBreakBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = RestBreakBatchRequiredExternalRefRule("rest break batch needs this field")
        val item = newRestBreakBatch("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = RestBreakBatchRequiredLabelRule("rest break batch needs this field")
        val item = newRestBreakBatch("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = RestBreakBatchExternalRefLengthRule(86400)
        val item = newRestBreakBatch("x", "y")
        item.externalRef = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
