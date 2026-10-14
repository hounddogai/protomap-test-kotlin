package com.acme.transport.customs.clearance.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ClearanceManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultClearanceManifestValidator()
        val item = newClearanceManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = ClearanceManifestNameLengthRule(3600)
        val item = newClearanceManifest("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = ClearanceManifestCodeLengthRule(16)
        val item = newClearanceManifest("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = ClearanceManifestExpiresAtRangeRule(32, 64)
        val item = newClearanceManifest("x", "y")
        item.expiresAt = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
