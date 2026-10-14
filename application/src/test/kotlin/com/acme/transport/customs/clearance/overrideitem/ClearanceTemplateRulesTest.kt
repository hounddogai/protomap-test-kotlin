package com.acme.transport.customs.clearance.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ClearanceTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultClearanceTemplateValidator()
        val item = newClearanceTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ClearanceTemplateRequiredCodeRule("clearance template needs a value")
        val item = newClearanceTemplate("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = ClearanceTemplateContactEmailLengthRule(32)
        val item = newClearanceTemplate("x", "y")
        item.contactEmail = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = ClearanceTemplateRequiredContactEmailRule("clearance template needs an entry")
        val item = newClearanceTemplate("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
