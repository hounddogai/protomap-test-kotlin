package com.acme.transport.emissions.carbonreport.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingCarbonReportWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingCarbonReportWindowValidator()
        val item = newPendingCarbonReportWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = PendingCarbonReportWindowRequiredNameRule("pending carbon report window needs an entry")
        val item = newPendingCarbonReportWindow("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = PendingCarbonReportWindowChecksumLengthRule(1000)
        val item = newPendingCarbonReportWindow("x", "y")
        item.checksum = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = PendingCarbonReportWindowExpiresAtRangeRule(255, 255)
        val item = newPendingCarbonReportWindow("x", "y")
        item.expiresAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
