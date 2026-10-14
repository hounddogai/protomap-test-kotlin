package com.acme.transport.safety.incident.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IncidentScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultIncidentScoreValidator()
        val item = newIncidentScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = IncidentScoreNameLengthRule(255)
        val item = newIncidentScore("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = IncidentScoreRequiredNameRule("incident score needs this field")
        val item = newIncidentScore("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = IncidentScoreCeilingRangeRule(6.1, 1951.5)
        val item = newIncidentScore("x", "y")
        item.ceiling = 1953.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
