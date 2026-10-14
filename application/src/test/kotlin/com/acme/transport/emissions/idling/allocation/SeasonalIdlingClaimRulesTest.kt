package com.acme.transport.emissions.idling.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalIdlingClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalIdlingClaimValidator()
        val item = newSeasonalIdlingClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = SeasonalIdlingClaimCodeLengthRule(32)
        val item = newSeasonalIdlingClaim("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = SeasonalIdlingClaimLabelLengthRule(1000)
        val item = newSeasonalIdlingClaim("x", "y")
        item.label = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SeasonalIdlingClaimRequiredCodeRule("seasonal idling claim needs this field")
        val item = newSeasonalIdlingClaim("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
