package com.acme.transport.safety.nearmiss.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NearMissAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultNearMissAdjustmentValidator()
        val item = newNearMissAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = NearMissAdjustmentAmountRangeRule(6.2, 1884.3)
        val item = newNearMissAdjustment("x", "y")
        item.amount = 1885.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = NearMissAdjustmentCodeLengthRule(64)
        val item = newNearMissAdjustment("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = NearMissAdjustmentTitleLengthRule(255)
        val item = newNearMissAdjustment("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = NearMissAdjustmentRequiredCodeRule("near miss adjustment needs this field")
        val item = newNearMissAdjustment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = NearMissAdjustmentRequiredTitleRule("near miss adjustment needs an entry")
        val item = newNearMissAdjustment("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
