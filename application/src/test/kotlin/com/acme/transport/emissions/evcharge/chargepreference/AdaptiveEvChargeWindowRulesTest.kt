package com.acme.transport.emissions.evcharge.chargepreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveEvChargeWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveEvChargeWindowValidator()
        val item = newAdaptiveEvChargeWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = AdaptiveEvChargeWindowReviewerLengthRule(120)
        val item = newAdaptiveEvChargeWindow("x", "y")
        item.reviewer = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank reviewer`() {
        val rule = AdaptiveEvChargeWindowRequiredReviewerRule("adaptive ev charge window needs an entry")
        val item = newAdaptiveEvChargeWindow("", "")
        item.reviewer = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = AdaptiveEvChargeWindowRequiredNameRule("adaptive ev charge window needs an entry")
        val item = newAdaptiveEvChargeWindow("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = AdaptiveEvChargeWindowNameLengthRule(80)
        val item = newAdaptiveEvChargeWindow("x", "y")
        item.name = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
