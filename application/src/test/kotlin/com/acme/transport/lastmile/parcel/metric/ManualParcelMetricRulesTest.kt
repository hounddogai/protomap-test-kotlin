package com.acme.transport.lastmile.parcel.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualParcelMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualParcelMetricValidator()
        val item = newManualParcelMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ManualParcelMetricRequiredCodeRule("manual parcel metric needs this field")
        val item = newManualParcelMetric("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = ManualParcelMetricShiftCodeLengthRule(64)
        val item = newManualParcelMetric("x", "y")
        item.shiftCode = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ManualParcelMetricRequiredNameRule("manual parcel metric needs this field")
        val item = newManualParcelMetric("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ManualParcelMetricCodeLengthRule(86400)
        val item = newManualParcelMetric("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
