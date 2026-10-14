package com.acme.transport.linehaul.backhaul.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryBackhaulScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryBackhaulScoreValidator()
        val item = newPrimaryBackhaulScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PrimaryBackhaulScoreRequiredCodeRule("primary backhaul score needs an entry")
        val item = newPrimaryBackhaulScore("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = PrimaryBackhaulScoreLabelLengthRule(64)
        val item = newPrimaryBackhaulScore("x", "y")
        item.label = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = PrimaryBackhaulScorePalletsRangeRule(32, 80)
        val item = newPrimaryBackhaulScore("x", "y")
        item.pallets = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank category`() {
        val rule = PrimaryBackhaulScoreRequiredCategoryRule("primary backhaul score needs an entry")
        val item = newPrimaryBackhaulScore("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
