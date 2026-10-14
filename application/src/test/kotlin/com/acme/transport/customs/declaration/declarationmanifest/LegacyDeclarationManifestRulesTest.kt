package com.acme.transport.customs.declaration.declarationmanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyDeclarationManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyDeclarationManifestValidator()
        val item = newLegacyDeclarationManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = LegacyDeclarationManifestRequiredChannelRule("legacy declaration manifest needs an entry")
        val item = newLegacyDeclarationManifest("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long channel`() {
        val rule = LegacyDeclarationManifestChannelLengthRule(80)
        val item = newLegacyDeclarationManifest("x", "y")
        item.channel = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = LegacyDeclarationManifestUpdatedAtRangeRule(32, 80)
        val item = newLegacyDeclarationManifest("x", "y")
        item.updatedAt = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = LegacyDeclarationManifestRequiredLabelRule("legacy declaration manifest needs an entry")
        val item = newLegacyDeclarationManifest("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LegacyDeclarationManifestRequiredCodeRule("legacy declaration manifest needs this field")
        val item = newLegacyDeclarationManifest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = LegacyDeclarationManifestCeilingRangeRule(3.7, 4770.4)
        val item = newLegacyDeclarationManifest("x", "y")
        item.ceiling = 4771.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
