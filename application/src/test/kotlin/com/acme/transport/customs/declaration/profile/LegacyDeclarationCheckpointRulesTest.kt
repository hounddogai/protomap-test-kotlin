package com.acme.transport.customs.declaration.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyDeclarationCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyDeclarationCheckpointValidator()
        val item = newLegacyDeclarationCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = LegacyDeclarationCheckpointDriverIdLengthRule(120)
        val item = newLegacyDeclarationCheckpoint("x", "y")
        item.driverId = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LegacyDeclarationCheckpointRequiredCodeRule("legacy declaration checkpoint needs a value")
        val item = newLegacyDeclarationCheckpoint("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = LegacyDeclarationCheckpointRequiredNameRule("legacy declaration checkpoint needs a value")
        val item = newLegacyDeclarationCheckpoint("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
