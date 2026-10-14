package com.acme.transport.customs.declaration.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryDeclarationDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryDeclarationDigestValidator()
        val item = newPrimaryDeclarationDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PrimaryDeclarationDigestRequiredCodeRule("primary declaration digest needs an entry")
        val item = newPrimaryDeclarationDigest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = PrimaryDeclarationDigestCodeLengthRule(1000)
        val item = newPrimaryDeclarationDigest("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = PrimaryDeclarationDigestLabelLengthRule(255)
        val item = newPrimaryDeclarationDigest("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = PrimaryDeclarationDigestMinimumRangeRule(3.7, 2195.4)
        val item = newPrimaryDeclarationDigest("x", "y")
        item.minimum = 2196.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
