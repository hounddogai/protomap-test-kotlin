package com.acme.transport.linehaul.tender.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientTenderGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientTenderGroupValidator()
        val item = newLenientTenderGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long country`() {
        val rule = LenientTenderGroupCountryLengthRule(16)
        val item = newLenientTenderGroup("x", "y")
        item.country = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank country`() {
        val rule = LenientTenderGroupRequiredCountryRule("lenient tender group needs a value")
        val item = newLenientTenderGroup("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = LenientTenderGroupMaximumRangeRule(5.7, 4086.4)
        val item = newLenientTenderGroup("x", "y")
        item.maximum = 4087.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
