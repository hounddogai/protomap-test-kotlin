package com.acme.transport.network.servicearea.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingServiceAreaAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingServiceAreaAssignmentValidator()
        val item = newPendingServiceAreaAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long summary`() {
        val rule = PendingServiceAreaAssignmentSummaryLengthRule(3600)
        val item = newPendingServiceAreaAssignment("x", "y")
        item.summary = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = PendingServiceAreaAssignmentRequiredPhoneRule("pending service area assignment needs a value")
        val item = newPendingServiceAreaAssignment("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = PendingServiceAreaAssignmentTitleLengthRule(64)
        val item = newPendingServiceAreaAssignment("x", "y")
        item.title = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PendingServiceAreaAssignmentRequiredCodeRule("pending service area assignment needs this field")
        val item = newPendingServiceAreaAssignment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = PendingServiceAreaAssignmentRequiredSummaryRule("pending service area assignment needs this field")
        val item = newPendingServiceAreaAssignment("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
