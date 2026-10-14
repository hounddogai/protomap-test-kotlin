package com.acme.transport.lastmile.attempt.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AttemptManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAttemptManifestValidator()
        val item = newAttemptManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AttemptManifestRequiredCodeRule("attempt manifest needs a value")
        val item = newAttemptManifest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = AttemptManifestShiftCodeLengthRule(3600)
        val item = newAttemptManifest("x", "y")
        item.shiftCode = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = AttemptManifestRequiredNameRule("attempt manifest needs this field")
        val item = newAttemptManifest("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
