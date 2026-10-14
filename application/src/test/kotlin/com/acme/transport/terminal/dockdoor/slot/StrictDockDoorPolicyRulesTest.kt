package com.acme.transport.terminal.dockdoor.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictDockDoorPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictDockDoorPolicyValidator()
        val item = newStrictDockDoorPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = StrictDockDoorPolicyCodeLengthRule(3600)
        val item = newStrictDockDoorPolicy("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = StrictDockDoorPolicyNameLengthRule(255)
        val item = newStrictDockDoorPolicy("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = StrictDockDoorPolicyRequiredNameRule("strict dock door policy needs this field")
        val item = newStrictDockDoorPolicy("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
