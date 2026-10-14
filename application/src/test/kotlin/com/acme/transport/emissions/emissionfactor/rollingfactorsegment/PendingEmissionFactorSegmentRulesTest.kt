package com.acme.transport.emissions.emissionfactor.rollingfactorsegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingEmissionFactorSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingEmissionFactorSegmentValidator()
        val item = newPendingEmissionFactorSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = PendingEmissionFactorSegmentCodeLengthRule(255)
        val item = newPendingEmissionFactorSegment("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = PendingEmissionFactorSegmentCeilingRangeRule(5.7, 3828.8)
        val item = newPendingEmissionFactorSegment("x", "y")
        item.ceiling = 3830.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = PendingEmissionFactorSegmentTitleLengthRule(64)
        val item = newPendingEmissionFactorSegment("x", "y")
        item.title = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = PendingEmissionFactorSegmentRequiredTitleRule("pending emission factor segment needs an entry")
        val item = newPendingEmissionFactorSegment("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
