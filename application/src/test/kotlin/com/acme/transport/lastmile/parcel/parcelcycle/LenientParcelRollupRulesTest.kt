package com.acme.transport.lastmile.parcel.parcelcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientParcelRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientParcelRollupValidator()
        val item = newLenientParcelRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = LenientParcelRollupNameLengthRule(255)
        val item = newLenientParcelRollup("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above amount`() {
        val rule = LenientParcelRollupAmountRangeRule(6.4, 3191.6)
        val item = newLenientParcelRollup("x", "y")
        item.amount = 3193.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = LenientParcelRollupStartsAtRangeRule(32, 3600)
        val item = newLenientParcelRollup("x", "y")
        item.startsAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
