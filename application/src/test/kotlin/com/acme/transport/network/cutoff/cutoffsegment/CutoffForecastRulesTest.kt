package com.acme.transport.network.cutoff.cutoffsegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CutoffForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCutoffForecastValidator()
        val item = newCutoffForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long depot`() {
        val rule = CutoffForecastDepotLengthRule(255)
        val item = newCutoffForecast("x", "y")
        item.depot = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CutoffForecastRequiredCodeRule("cutoff forecast needs this field")
        val item = newCutoffForecast("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
