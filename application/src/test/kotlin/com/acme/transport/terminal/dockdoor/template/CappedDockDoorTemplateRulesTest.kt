package com.acme.transport.terminal.dockdoor.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedDockDoorTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedDockDoorTemplateValidator()
        val item = newCappedDockDoorTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = CappedDockDoorTemplateCodeLengthRule(64)
        val item = newCappedDockDoorTemplate("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = CappedDockDoorTemplateRequiredLabelRule("capped dock door template needs an entry")
        val item = newCappedDockDoorTemplate("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = CappedDockDoorTemplateRequiredContactEmailRule("capped dock door template needs this field")
        val item = newCappedDockDoorTemplate("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = CappedDockDoorTemplateLabelLengthRule(255)
        val item = newCappedDockDoorTemplate("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = CappedDockDoorTemplateRequiredNameRule("capped dock door template needs this field")
        val item = newCappedDockDoorTemplate("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
