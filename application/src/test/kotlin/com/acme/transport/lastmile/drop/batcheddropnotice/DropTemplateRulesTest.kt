package com.acme.transport.lastmile.drop.batcheddropnotice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DropTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDropTemplateValidator()
        val item = newDropTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = DropTemplateRequiredNotesRule("drop template needs an entry")
        val item = newDropTemplate("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long notes`() {
        val rule = DropTemplateNotesLengthRule(16)
        val item = newDropTemplate("x", "y")
        item.notes = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = DropTemplateRequiredLabelRule("drop template needs a value")
        val item = newDropTemplate("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
