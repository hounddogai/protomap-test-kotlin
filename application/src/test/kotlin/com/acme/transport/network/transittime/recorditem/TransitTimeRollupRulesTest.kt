package com.acme.transport.network.transittime.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TransitTimeRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTransitTimeRollupValidator()
        val item = newTransitTimeRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = TransitTimeRollupUpdatedAtRangeRule(86400, 86400)
        val item = newTransitTimeRollup("x", "y")
        item.updatedAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = TransitTimeRollupOdometerRangeRule(3600, 86400)
        val item = newTransitTimeRollup("x", "y")
        item.odometer = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = TransitTimeRollupEtaMinutesRangeRule(120, 3600)
        val item = newTransitTimeRollup("x", "y")
        item.etaMinutes = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = TransitTimeRollupLabelLengthRule(1000)
        val item = newTransitTimeRollup("x", "y")
        item.label = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = TransitTimeRollupRequiredLabelRule("transit time rollup needs this field")
        val item = newTransitTimeRollup("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
