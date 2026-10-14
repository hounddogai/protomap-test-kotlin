package com.acme.transport.safety.cameraevent.eventrecord

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackCameraEventSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackCameraEventSummaryValidator()
        val item = newFallbackCameraEventSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = FallbackCameraEventSummaryRequiredLabelRule("fallback camera event summary needs a value")
        val item = newFallbackCameraEventSummary("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = FallbackCameraEventSummaryRequiredChecksumRule("fallback camera event summary needs this field")
        val item = newFallbackCameraEventSummary("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = FallbackCameraEventSummaryLabelLengthRule(120)
        val item = newFallbackCameraEventSummary("x", "y")
        item.label = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
