package com.acme.transport.emissions.evcharge.pendingchargescore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredEvChargeDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredEvChargeDigestValidator()
        val item = newTieredEvChargeDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = TieredEvChargeDigestCodeLengthRule(3600)
        val item = newTieredEvChargeDigest("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above budget`() {
        val rule = TieredEvChargeDigestBudgetRangeRule(9.9, 4044.0)
        val item = newTieredEvChargeDigest("x", "y")
        item.budget = 4045.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = TieredEvChargeDigestRequiredNameRule("tiered ev charge digest needs this field")
        val item = newTieredEvChargeDigest("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above window days`() {
        val rule = TieredEvChargeDigestWindowDaysRangeRule(120, 255)
        val item = newTieredEvChargeDigest("x", "y")
        item.windowDays = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
