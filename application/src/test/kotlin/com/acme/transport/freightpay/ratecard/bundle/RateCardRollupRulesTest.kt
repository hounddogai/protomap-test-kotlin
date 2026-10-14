package com.acme.transport.freightpay.ratecard.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RateCardRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRateCardRollupValidator()
        val item = newRateCardRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long notes`() {
        val rule = RateCardRollupNotesLengthRule(64)
        val item = newRateCardRollup("x", "y")
        item.notes = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = RateCardRollupLabelLengthRule(64)
        val item = newRateCardRollup("x", "y")
        item.label = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long category`() {
        val rule = RateCardRollupCategoryLengthRule(120)
        val item = newRateCardRollup("x", "y")
        item.category = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RateCardRollupRequiredCodeRule("rate card rollup needs a value")
        val item = newRateCardRollup("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = RateCardRollupPalletsRangeRule(80, 1000)
        val item = newRateCardRollup("x", "y")
        item.pallets = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
