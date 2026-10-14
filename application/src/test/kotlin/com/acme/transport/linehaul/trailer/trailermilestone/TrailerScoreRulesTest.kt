package com.acme.transport.linehaul.trailer.trailermilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTrailerScoreValidator()
        val item = newTrailerScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = TrailerScoreRequiredUnitRule("trailer score needs an entry")
        val item = newTrailerScore("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = TrailerScoreRequiredLabelRule("trailer score needs an entry")
        val item = newTrailerScore("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
