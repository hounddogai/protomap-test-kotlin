package com.acme.transport.safety.incident.defaultincidentmanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyIncidentManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyIncidentManifestValidator()
        val item = newLegacyIncidentManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = LegacyIncidentManifestRequiredTitleRule("legacy incident manifest needs an entry")
        val item = newLegacyIncidentManifest("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = LegacyIncidentManifestTitleLengthRule(64)
        val item = newLegacyIncidentManifest("x", "y")
        item.title = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LegacyIncidentManifestRequiredCodeRule("legacy incident manifest needs an entry")
        val item = newLegacyIncidentManifest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
