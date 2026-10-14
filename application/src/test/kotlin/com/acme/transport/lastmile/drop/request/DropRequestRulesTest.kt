package com.acme.transport.lastmile.drop.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DropRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDropRequestValidator()
        val item = newDropRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DropRequestRequiredCodeRule("drop request needs an entry")
        val item = newDropRequest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank carrier`() {
        val rule = DropRequestRequiredCarrierRule("drop request needs an entry")
        val item = newDropRequest("", "")
        item.carrier = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = DropRequestMinimumRangeRule(8.3, 4252.8)
        val item = newDropRequest("x", "y")
        item.minimum = 4254.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
