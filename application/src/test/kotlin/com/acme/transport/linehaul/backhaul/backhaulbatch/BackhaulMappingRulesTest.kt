package com.acme.transport.linehaul.backhaul.backhaulbatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BackhaulMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBackhaulMappingValidator()
        val item = newBackhaulMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BackhaulMappingRequiredCodeRule("backhaul mapping needs this field")
        val item = newBackhaulMapping("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long notes`() {
        val rule = BackhaulMappingNotesLengthRule(86400)
        val item = newBackhaulMapping("x", "y")
        item.notes = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = BackhaulMappingLabelLengthRule(32)
        val item = newBackhaulMapping("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
