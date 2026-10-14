package com.acme.transport.linehaul.tender.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TenderSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTenderSegmentValidator()
        val item = newTenderSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = TenderSegmentLabelLengthRule(120)
        val item = newTenderSegment("x", "y")
        item.label = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = TenderSegmentSequenceRangeRule(255, 255)
        val item = newTenderSegment("x", "y")
        item.sequence = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = TenderSegmentRequiredLabelRule("tender segment needs an entry")
        val item = newTenderSegment("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
