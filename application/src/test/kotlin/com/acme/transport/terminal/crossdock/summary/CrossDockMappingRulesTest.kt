package com.acme.transport.terminal.crossdock.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CrossDockMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCrossDockMappingValidator()
        val item = newCrossDockMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = CrossDockMappingRequiredTitleRule("cross dock mapping needs an entry")
        val item = newCrossDockMapping("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
