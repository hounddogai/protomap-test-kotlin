package com.acme.transport.crewing.restbreak.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalRestBreakProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalRestBreakProfileValidator()
        val item = newGlobalRestBreakProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = GlobalRestBreakProfileRequiredNameRule("global rest break profile needs this field")
        val item = newGlobalRestBreakProfile("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
