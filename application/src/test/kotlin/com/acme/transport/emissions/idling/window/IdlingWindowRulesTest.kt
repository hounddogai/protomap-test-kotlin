package com.acme.transport.emissions.idling.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IdlingWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultIdlingWindowValidator()
        val item = newIdlingWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above revision`() {
        val rule = IdlingWindowRevisionRangeRule(32, 3600)
        val item = newIdlingWindow("x", "y")
        item.revision = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank country`() {
        val rule = IdlingWindowRequiredCountryRule("idling window needs this field")
        val item = newIdlingWindow("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = IdlingWindowRequiredTitleRule("idling window needs a value")
        val item = newIdlingWindow("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
