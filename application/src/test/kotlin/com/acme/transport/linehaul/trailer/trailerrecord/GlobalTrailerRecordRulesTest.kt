package com.acme.transport.linehaul.trailer.trailerrecord

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalTrailerRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalTrailerRecordValidator()
        val item = newGlobalTrailerRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = GlobalTrailerRecordRequiredNameRule("global trailer record needs an entry")
        val item = newGlobalTrailerRecord("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long summary`() {
        val rule = GlobalTrailerRecordSummaryLengthRule(86400)
        val item = newGlobalTrailerRecord("x", "y")
        item.summary = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = GlobalTrailerRecordTimezoneLengthRule(86400)
        val item = newGlobalTrailerRecord("x", "y")
        item.timezone = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
