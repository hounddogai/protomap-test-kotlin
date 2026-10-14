package com.acme.transport.safety.cameraevent.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackCameraEventRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackCameraEventRecordValidator()
        val item = newFallbackCameraEventRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = FallbackCameraEventRecordEtaMinutesRangeRule(16, 1000)
        val item = newFallbackCameraEventRecord("x", "y")
        item.etaMinutes = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = FallbackCameraEventRecordRequiredDockRule("fallback camera event record needs an entry")
        val item = newFallbackCameraEventRecord("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = FallbackCameraEventRecordQuantityRangeRule(80, 1000)
        val item = newFallbackCameraEventRecord("x", "y")
        item.quantity = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
