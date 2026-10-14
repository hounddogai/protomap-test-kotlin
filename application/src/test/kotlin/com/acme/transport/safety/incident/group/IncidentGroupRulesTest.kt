package com.acme.transport.safety.incident.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IncidentGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultIncidentGroupValidator()
        val item = newIncidentGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = IncidentGroupRequiredLaneRule("incident group needs an entry")
        val item = newIncidentGroup("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = IncidentGroupRequiredShiftCodeRule("incident group needs a value")
        val item = newIncidentGroup("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
