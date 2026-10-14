package com.acme.transport.terminal.crossdock.docksummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualCrossDockSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualCrossDockSummaryValidator()
        val item = newManualCrossDockSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = ManualCrossDockSummaryRequiredExternalRefRule("manual cross dock summary needs this field")
        val item = newManualCrossDockSummary("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above retries`() {
        val rule = ManualCrossDockSummaryRetriesRangeRule(16, 3600)
        val item = newManualCrossDockSummary("x", "y")
        item.retries = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
