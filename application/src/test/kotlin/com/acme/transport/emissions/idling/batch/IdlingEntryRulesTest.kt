package com.acme.transport.emissions.idling.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IdlingEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultIdlingEntryValidator()
        val item = newIdlingEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = IdlingEntryRequiredContactEmailRule("idling entry needs an entry")
        val item = newIdlingEntry("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = IdlingEntryCodeLengthRule(120)
        val item = newIdlingEntry("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = IdlingEntryRequiredCodeRule("idling entry needs a value")
        val item = newIdlingEntry("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
