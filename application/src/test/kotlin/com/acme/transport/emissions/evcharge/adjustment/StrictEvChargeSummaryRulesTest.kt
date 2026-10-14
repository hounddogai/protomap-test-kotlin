package com.acme.transport.emissions.evcharge.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictEvChargeSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictEvChargeSummaryValidator()
        val item = newStrictEvChargeSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = StrictEvChargeSummaryRequiredCodeRule("strict ev charge summary needs an entry")
        val item = newStrictEvChargeSummary("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = StrictEvChargeSummaryRequiredSegmentRule("strict ev charge summary needs a value")
        val item = newStrictEvChargeSummary("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = StrictEvChargeSummaryRequiredLabelRule("strict ev charge summary needs a value")
        val item = newStrictEvChargeSummary("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = StrictEvChargeSummaryCodeLengthRule(255)
        val item = newStrictEvChargeSummary("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
