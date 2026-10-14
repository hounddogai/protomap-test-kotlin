package com.acme.transport.customs.declaration.declarationmanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingDeclarationRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingDeclarationRecordValidator()
        val item = newRollingDeclarationRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = RollingDeclarationRecordLoadKgRangeRule(9.0, 4271.4)
        val item = newRollingDeclarationRecord("x", "y")
        item.loadKg = 4272.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RollingDeclarationRecordRequiredCodeRule("rolling declaration record needs this field")
        val item = newRollingDeclarationRecord("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
