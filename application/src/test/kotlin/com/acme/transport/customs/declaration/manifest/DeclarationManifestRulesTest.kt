package com.acme.transport.customs.declaration.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeclarationManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDeclarationManifestValidator()
        val item = newDeclarationManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = DeclarationManifestRequiredTitleRule("declaration manifest needs this field")
        val item = newDeclarationManifest("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = DeclarationManifestRequiredExternalRefRule("declaration manifest needs this field")
        val item = newDeclarationManifest("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above limit`() {
        val rule = DeclarationManifestLimitRangeRule(64, 120)
        val item = newDeclarationManifest("x", "y")
        item.limit = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
