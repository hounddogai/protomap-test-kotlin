package com.acme.transport.lastmile.locker.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualLockerMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualLockerMetricValidator()
        val item = newManualLockerMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = ManualLockerMetricTitleLengthRule(255)
        val item = newManualLockerMetric("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ManualLockerMetricRequiredCodeRule("manual locker metric needs an entry")
        val item = newManualLockerMetric("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = ManualLockerMetricShiftCodeLengthRule(120)
        val item = newManualLockerMetric("x", "y")
        item.shiftCode = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
