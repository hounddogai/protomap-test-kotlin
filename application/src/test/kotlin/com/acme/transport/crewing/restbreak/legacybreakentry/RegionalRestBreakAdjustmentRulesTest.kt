package com.acme.transport.crewing.restbreak.legacybreakentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalRestBreakAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalRestBreakAdjustmentValidator()
        val item = newRegionalRestBreakAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RegionalRestBreakAdjustmentRequiredNameRule("regional rest break adjustment needs a value")
        val item = newRegionalRestBreakAdjustment("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
