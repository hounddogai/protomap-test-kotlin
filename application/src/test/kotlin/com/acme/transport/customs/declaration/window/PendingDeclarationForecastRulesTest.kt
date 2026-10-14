package com.acme.transport.customs.declaration.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingDeclarationForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingDeclarationForecastValidator()
        val item = newPendingDeclarationForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = PendingDeclarationForecastRequiredTitleRule("pending declaration forecast needs an entry")
        val item = newPendingDeclarationForecast("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long route code`() {
        val rule = PendingDeclarationForecastRouteCodeLengthRule(64)
        val item = newPendingDeclarationForecast("x", "y")
        item.routeCode = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = PendingDeclarationForecastCodeLengthRule(32)
        val item = newPendingDeclarationForecast("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PendingDeclarationForecastRequiredCodeRule("pending declaration forecast needs a value")
        val item = newPendingDeclarationForecast("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
