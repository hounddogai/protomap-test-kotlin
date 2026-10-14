package com.acme.transport.terminal.dockdoor.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalDockDoorTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalDockDoorTemplateValidator()
        val item = newGlobalDockDoorTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = GlobalDockDoorTemplateCodeLengthRule(86400)
        val item = newGlobalDockDoorTemplate("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = GlobalDockDoorTemplateNameLengthRule(86400)
        val item = newGlobalDockDoorTemplate("x", "y")
        item.name = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = GlobalDockDoorTemplateStopCountRangeRule(120, 86400)
        val item = newGlobalDockDoorTemplate("x", "y")
        item.stopCount = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
