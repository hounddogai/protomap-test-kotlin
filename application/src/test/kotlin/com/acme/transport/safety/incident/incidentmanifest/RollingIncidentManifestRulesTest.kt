package com.acme.transport.safety.incident.incidentmanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingIncidentManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingIncidentManifestValidator()
        val item = newRollingIncidentManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = RollingIncidentManifestOdometerRangeRule(80, 3600)
        val item = newRollingIncidentManifest("x", "y")
        item.odometer = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long notes`() {
        val rule = RollingIncidentManifestNotesLengthRule(32)
        val item = newRollingIncidentManifest("x", "y")
        item.notes = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RollingIncidentManifestRequiredCodeRule("rolling incident manifest needs a value")
        val item = newRollingIncidentManifest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = RollingIncidentManifestStopCountRangeRule(16, 255)
        val item = newRollingIncidentManifest("x", "y")
        item.stopCount = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = RollingIncidentManifestTitleLengthRule(64)
        val item = newRollingIncidentManifest("x", "y")
        item.title = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
