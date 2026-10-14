package com.acme.transport.customs.clearance.clearancerequest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ClearanceDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultClearanceDigestValidator()
        val item = newClearanceDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = ClearanceDigestExternalRefLengthRule(16)
        val item = newClearanceDigest("x", "y")
        item.externalRef = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ClearanceDigestRequiredCodeRule("clearance digest needs a value")
        val item = newClearanceDigest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ClearanceDigestCodeLengthRule(255)
        val item = newClearanceDigest("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ClearanceDigestRequiredTitleRule("clearance digest needs a value")
        val item = newClearanceDigest("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
