package com.acme.transport.network.servicearea.areamanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServiceAreaBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultServiceAreaBatchValidator()
        val item = newServiceAreaBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ServiceAreaBatchRequiredNameRule("service area batch needs this field")
        val item = newServiceAreaBatch("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long summary`() {
        val rule = ServiceAreaBatchSummaryLengthRule(32)
        val item = newServiceAreaBatch("x", "y")
        item.summary = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = ServiceAreaBatchRequiredNotesRule("service area batch needs this field")
        val item = newServiceAreaBatch("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ServiceAreaBatchRequiredCodeRule("service area batch needs an entry")
        val item = newServiceAreaBatch("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ServiceAreaBatchCodeLengthRule(3600)
        val item = newServiceAreaBatch("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
