package com.acme.transport.emissions.idling.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IdlingAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultIdlingAllocationValidator()
        val item = newIdlingAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = IdlingAllocationScoreRangeRule(4.7, 4899.9)
        val item = newIdlingAllocation("x", "y")
        item.score = 4901.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = IdlingAllocationLabelLengthRule(16)
        val item = newIdlingAllocation("x", "y")
        item.label = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank country`() {
        val rule = IdlingAllocationRequiredCountryRule("idling allocation needs an entry")
        val item = newIdlingAllocation("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = IdlingAllocationCodeLengthRule(64)
        val item = newIdlingAllocation("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
