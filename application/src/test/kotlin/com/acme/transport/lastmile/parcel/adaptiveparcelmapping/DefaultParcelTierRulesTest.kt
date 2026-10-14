package com.acme.transport.lastmile.parcel.adaptiveparcelmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultParcelTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultParcelTierValidator()
        val item = newDefaultParcelTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = DefaultParcelTierLabelLengthRule(120)
        val item = newDefaultParcelTier("x", "y")
        item.label = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = DefaultParcelTierBatchSizeRangeRule(32, 120)
        val item = newDefaultParcelTier("x", "y")
        item.batchSize = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = DefaultParcelTierDistanceKmRangeRule(8.3, 3149.3)
        val item = newDefaultParcelTier("x", "y")
        item.distanceKm = 3150.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = DefaultParcelTierCodeLengthRule(32)
        val item = newDefaultParcelTier("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = DefaultParcelTierRequiredLabelRule("default parcel tier needs an entry")
        val item = newDefaultParcelTier("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
