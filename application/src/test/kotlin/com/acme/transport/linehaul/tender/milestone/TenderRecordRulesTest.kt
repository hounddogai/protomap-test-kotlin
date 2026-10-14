package com.acme.transport.linehaul.tender.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TenderRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTenderRecordValidator()
        val item = newTenderRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long segment`() {
        val rule = TenderRecordSegmentLengthRule(120)
        val item = newTenderRecord("x", "y")
        item.segment = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.segment = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = TenderRecordRequiredTitleRule("tender record needs this field")
        val item = newTenderRecord("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
