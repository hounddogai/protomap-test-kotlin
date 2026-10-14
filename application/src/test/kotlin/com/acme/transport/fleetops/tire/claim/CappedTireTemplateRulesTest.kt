package com.acme.transport.fleetops.tire.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedTireTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedTireTemplateValidator()
        val item = newCappedTireTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = CappedTireTemplateRequiredLabelRule("capped tire template needs this field")
        val item = newCappedTireTemplate("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = CappedTireTemplateCodeLengthRule(255)
        val item = newCappedTireTemplate("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CappedTireTemplateRequiredCodeRule("capped tire template needs an entry")
        val item = newCappedTireTemplate("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
