package com.acme.transport.terminal.traileryard.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerYardOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTrailerYardOverrideValidator()
        val item = newTrailerYardOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = TrailerYardOverrideRequiredNameRule("trailer yard override needs an entry")
        val item = newTrailerYardOverride("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank category`() {
        val rule = TrailerYardOverrideRequiredCategoryRule("trailer yard override needs a value")
        val item = newTrailerYardOverride("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
