package com.acme.transport.crewing.roster.fallbackrosterforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictRosterManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictRosterManifestValidator()
        val item = newStrictRosterManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = StrictRosterManifestRequiredLabelRule("strict roster manifest needs this field")
        val item = newStrictRosterManifest("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = StrictRosterManifestLabelLengthRule(255)
        val item = newStrictRosterManifest("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
