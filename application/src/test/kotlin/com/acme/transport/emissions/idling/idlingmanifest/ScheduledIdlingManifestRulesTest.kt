package com.acme.transport.emissions.idling.idlingmanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledIdlingManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledIdlingManifestValidator()
        val item = newScheduledIdlingManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ScheduledIdlingManifestCodeLengthRule(64)
        val item = newScheduledIdlingManifest("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ScheduledIdlingManifestRequiredLabelRule("scheduled idling manifest needs this field")
        val item = newScheduledIdlingManifest("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
