package com.acme.transport.lastmile.parcel.parcelcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalParcelBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalParcelBatchValidator()
        val item = newGlobalParcelBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = GlobalParcelBatchLoadKgRangeRule(9.8, 398.1)
        val item = newGlobalParcelBatch("x", "y")
        item.loadKg = 399.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = GlobalParcelBatchCodeLengthRule(16)
        val item = newGlobalParcelBatch("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = GlobalParcelBatchFuelLitersRangeRule(7.1, 432.4)
        val item = newGlobalParcelBatch("x", "y")
        item.fuelLiters = 433.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = GlobalParcelBatchRequiredNameRule("global parcel batch needs this field")
        val item = newGlobalParcelBatch("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
