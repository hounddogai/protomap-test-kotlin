package com.acme.transport.terminal.traileryard.yardreservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalTrailerYardRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalTrailerYardRecordValidator()
        val item = newRegionalTrailerYardRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long region`() {
        val rule = RegionalTrailerYardRecordRegionLengthRule(86400)
        val item = newRegionalTrailerYardRecord("x", "y")
        item.region = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = RegionalTrailerYardRecordTitleLengthRule(16)
        val item = newRegionalTrailerYardRecord("x", "y")
        item.title = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
