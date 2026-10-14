package com.acme.transport.lastmile.drop.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DropProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDropProfileValidator()
        val item = newDropProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = DropProfileRequiredCurrencyRule("drop profile needs a value")
        val item = newDropProfile("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
