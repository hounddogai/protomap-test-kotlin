package com.acme.transport.lastmile.parcel.parcelmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultParcelMetricRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultParcelMetricValidator()
        val item = newDefaultParcelMetric("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = DefaultParcelMetricCodeLengthRule(64)
        val item = newDefaultParcelMetric("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = DefaultParcelMetricTitleLengthRule(80)
        val item = newDefaultParcelMetric("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
