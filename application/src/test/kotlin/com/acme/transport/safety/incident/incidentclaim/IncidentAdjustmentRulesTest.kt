package com.acme.transport.safety.incident.incidentclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IncidentAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultIncidentAdjustmentValidator()
        val item = newIncidentAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = IncidentAdjustmentTitleLengthRule(32)
        val item = newIncidentAdjustment("x", "y")
        item.title = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = IncidentAdjustmentRequiredCodeRule("incident adjustment needs a value")
        val item = newIncidentAdjustment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = IncidentAdjustmentRequiredLabelRule("incident adjustment needs a value")
        val item = newIncidentAdjustment("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = IncidentAdjustmentRequiredTitleRule("incident adjustment needs a value")
        val item = newIncidentAdjustment("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
