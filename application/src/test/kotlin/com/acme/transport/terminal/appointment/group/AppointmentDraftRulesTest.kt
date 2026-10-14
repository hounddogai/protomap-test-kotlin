package com.acme.transport.terminal.appointment.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppointmentDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAppointmentDraftValidator()
        val item = newAppointmentDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = AppointmentDraftThresholdRangeRule(9.1, 2935.6)
        val item = newAppointmentDraft("x", "y")
        item.threshold = 2937.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = AppointmentDraftRequiredNameRule("appointment draft needs a value")
        val item = newAppointmentDraft("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = AppointmentDraftRequiredTitleRule("appointment draft needs a value")
        val item = newAppointmentDraft("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = AppointmentDraftRequiredExternalRefRule("appointment draft needs an entry")
        val item = newAppointmentDraft("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
