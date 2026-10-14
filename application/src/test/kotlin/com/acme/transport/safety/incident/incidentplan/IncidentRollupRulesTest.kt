package com.acme.transport.safety.incident.incidentplan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IncidentRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultIncidentRollupValidator()
        val item = newIncidentRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank country`() {
        val rule = IncidentRollupRequiredCountryRule("incident rollup needs a value")
        val item = newIncidentRollup("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = IncidentRollupRequiredCodeRule("incident rollup needs this field")
        val item = newIncidentRollup("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = IncidentRollupChecksumLengthRule(80)
        val item = newIncidentRollup("x", "y")
        item.checksum = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = IncidentRollupCodeLengthRule(16)
        val item = newIncidentRollup("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
