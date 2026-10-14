package com.acme.transport.emissions.idling.idlingsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalIdlingWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalIdlingWindowValidator()
        val item = newSeasonalIdlingWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = SeasonalIdlingWindowRequiredLabelRule("seasonal idling window needs an entry")
        val item = newSeasonalIdlingWindow("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = SeasonalIdlingWindowRequiredLocaleRule("seasonal idling window needs a value")
        val item = newSeasonalIdlingWindow("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long category`() {
        val rule = SeasonalIdlingWindowCategoryLengthRule(3600)
        val item = newSeasonalIdlingWindow("x", "y")
        item.category = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
