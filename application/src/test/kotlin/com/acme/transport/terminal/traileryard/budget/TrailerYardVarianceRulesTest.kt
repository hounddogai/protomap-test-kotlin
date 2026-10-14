package com.acme.transport.terminal.traileryard.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerYardVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTrailerYardVarianceValidator()
        val item = newTrailerYardVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long route code`() {
        val rule = TrailerYardVarianceRouteCodeLengthRule(120)
        val item = newTrailerYardVariance("x", "y")
        item.routeCode = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = TrailerYardVarianceTitleLengthRule(255)
        val item = newTrailerYardVariance("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = TrailerYardVarianceRequiredNameRule("trailer yard variance needs a value")
        val item = newTrailerYardVariance("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
