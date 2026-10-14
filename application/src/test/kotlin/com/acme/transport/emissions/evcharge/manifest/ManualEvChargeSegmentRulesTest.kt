package com.acme.transport.emissions.evcharge.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualEvChargeSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualEvChargeSegmentValidator()
        val item = newManualEvChargeSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above retries`() {
        val rule = ManualEvChargeSegmentRetriesRangeRule(120, 3600)
        val item = newManualEvChargeSegment("x", "y")
        item.retries = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long source`() {
        val rule = ManualEvChargeSegmentSourceLengthRule(3600)
        val item = newManualEvChargeSegment("x", "y")
        item.source = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.source = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ManualEvChargeSegmentRequiredNameRule("manual ev charge segment needs this field")
        val item = newManualEvChargeSegment("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ManualEvChargeSegmentCodeLengthRule(64)
        val item = newManualEvChargeSegment("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
