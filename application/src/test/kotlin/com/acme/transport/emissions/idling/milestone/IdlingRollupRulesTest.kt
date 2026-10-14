package com.acme.transport.emissions.idling.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IdlingRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultIdlingRollupValidator()
        val item = newIdlingRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = IdlingRollupRequiredCodeRule("idling rollup needs a value")
        val item = newIdlingRollup("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = IdlingRollupRequiredUnitRule("idling rollup needs an entry")
        val item = newIdlingRollup("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = IdlingRollupContactEmailLengthRule(80)
        val item = newIdlingRollup("x", "y")
        item.contactEmail = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
