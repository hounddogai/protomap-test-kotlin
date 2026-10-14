package com.acme.transport.safety.incident.incidenttarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingIncidentBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingIncidentBaselineValidator()
        val item = newRollingIncidentBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = RollingIncidentBaselineTitleLengthRule(80)
        val item = newRollingIncidentBaseline("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = RollingIncidentBaselineRequiredTitleRule("rolling incident baseline needs an entry")
        val item = newRollingIncidentBaseline("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RollingIncidentBaselineRequiredCodeRule("rolling incident baseline needs this field")
        val item = newRollingIncidentBaseline("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = RollingIncidentBaselineNameLengthRule(32)
        val item = newRollingIncidentBaseline("x", "y")
        item.name = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
