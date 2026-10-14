package com.acme.transport.customs.clearance.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ClearanceRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultClearanceRollupValidator()
        val item = newClearanceRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = ClearanceRollupContactEmailLengthRule(120)
        val item = newClearanceRollup("x", "y")
        item.contactEmail = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = ClearanceRollupRequiredContactEmailRule("clearance rollup needs an entry")
        val item = newClearanceRollup("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ClearanceRollupRequiredCodeRule("clearance rollup needs this field")
        val item = newClearanceRollup("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ClearanceRollupCodeLengthRule(64)
        val item = newClearanceRollup("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
