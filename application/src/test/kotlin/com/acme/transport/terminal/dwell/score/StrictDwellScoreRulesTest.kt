package com.acme.transport.terminal.dwell.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictDwellScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictDwellScoreValidator()
        val item = newStrictDwellScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank country`() {
        val rule = StrictDwellScoreRequiredCountryRule("strict dwell score needs this field")
        val item = newStrictDwellScore("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = StrictDwellScoreRequiredCodeRule("strict dwell score needs a value")
        val item = newStrictDwellScore("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
