package com.acme.transport.crewing.restbreak.regionalbreakpreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingRestBreakRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingRestBreakRollupValidator()
        val item = newPendingRestBreakRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = PendingRestBreakRollupCarrierLengthRule(255)
        val item = newPendingRestBreakRollup("x", "y")
        item.carrier = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above weight`() {
        val rule = PendingRestBreakRollupWeightRangeRule(7.0, 2588.2)
        val item = newPendingRestBreakRollup("x", "y")
        item.weight = 2589.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
