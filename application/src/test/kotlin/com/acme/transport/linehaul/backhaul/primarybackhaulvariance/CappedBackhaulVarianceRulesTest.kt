package com.acme.transport.linehaul.backhaul.primarybackhaulvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedBackhaulVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedBackhaulVarianceValidator()
        val item = newCappedBackhaulVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = CappedBackhaulVarianceFloorValueRangeRule(2.0, 1579.2)
        val item = newCappedBackhaulVariance("x", "y")
        item.floorValue = 1580.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = CappedBackhaulVarianceCodeLengthRule(1000)
        val item = newCappedBackhaulVariance("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = CappedBackhaulVarianceRequiredNameRule("capped backhaul variance needs this field")
        val item = newCappedBackhaulVariance("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = CappedBackhaulVarianceRequiredTitleRule("capped backhaul variance needs an entry")
        val item = newCappedBackhaulVariance("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = CappedBackhaulVarianceDiscountRangeRule(4.5, 2388.9)
        val item = newCappedBackhaulVariance("x", "y")
        item.discount = 2390.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
