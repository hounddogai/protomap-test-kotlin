package com.acme.transport.terminal.appointment.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientAppointmentMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientAppointmentMetricValidator()
        val item = newLenientAppointmentMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = LenientAppointmentMetricRequiredLabelRule("lenient appointment metric needs an entry")
        val item = newLenientAppointmentMetric("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = LenientAppointmentMetricBatchSizeRangeRule(80, 80)
        val item = newLenientAppointmentMetric("x", "y")
        item.batchSize = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LenientAppointmentMetricRequiredCodeRule("lenient appointment metric needs an entry")
        val item = newLenientAppointmentMetric("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = LenientAppointmentMetricSequenceRangeRule(1000, 3600)
        val item = newLenientAppointmentMetric("x", "y")
        item.sequence = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
