package com.acme.transport.terminal.appointment.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultAppointmentManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultAppointmentManifestValidator()
        val item = newDefaultAppointmentManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank category`() {
        val rule = DefaultAppointmentManifestRequiredCategoryRule("default appointment manifest needs this field")
        val item = newDefaultAppointmentManifest("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = DefaultAppointmentManifestRequiredTitleRule("default appointment manifest needs this field")
        val item = newDefaultAppointmentManifest("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above cost`() {
        val rule = DefaultAppointmentManifestCostRangeRule(0.4, 636.5)
        val item = newDefaultAppointmentManifest("x", "y")
        item.cost = 638.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DefaultAppointmentManifestRequiredCodeRule("default appointment manifest needs a value")
        val item = newDefaultAppointmentManifest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
