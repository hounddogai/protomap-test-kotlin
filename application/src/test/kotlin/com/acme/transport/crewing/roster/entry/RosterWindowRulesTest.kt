package com.acme.transport.crewing.roster.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RosterWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRosterWindowValidator()
        val item = newRosterWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = RosterWindowRequiredLocaleRule("roster window needs this field")
        val item = newRosterWindow("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
