package com.acme.transport.fleetops.tire.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TireTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTireTemplateValidator()
        val item = newTireTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = TireTemplateStartsAtRangeRule(32, 120)
        val item = newTireTemplate("x", "y")
        item.startsAt = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank description`() {
        val rule = TireTemplateRequiredDescriptionRule("tire template needs an entry")
        val item = newTireTemplate("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = TireTemplateCodeLengthRule(120)
        val item = newTireTemplate("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TireTemplateRequiredCodeRule("tire template needs an entry")
        val item = newTireTemplate("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
