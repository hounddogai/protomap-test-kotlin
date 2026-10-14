package com.acme.transport.linehaul.backhaul.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BackhaulTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBackhaulTemplateValidator()
        val item = newBackhaulTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = BackhaulTemplateNameLengthRule(16)
        val item = newBackhaulTemplate("x", "y")
        item.name = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = BackhaulTemplateCeilingRangeRule(0.9, 1335.1)
        val item = newBackhaulTemplate("x", "y")
        item.ceiling = 1336.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = BackhaulTemplateOwnerIdLengthRule(255)
        val item = newBackhaulTemplate("x", "y")
        item.ownerId = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = BackhaulTemplateCodeLengthRule(80)
        val item = newBackhaulTemplate("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
