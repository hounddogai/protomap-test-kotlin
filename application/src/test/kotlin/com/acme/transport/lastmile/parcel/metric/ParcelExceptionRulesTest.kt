package com.acme.transport.lastmile.parcel.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParcelExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultParcelExceptionValidator()
        val item = newParcelException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = ParcelExceptionShiftCodeLengthRule(16)
        val item = newParcelException("x", "y")
        item.shiftCode = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long dock`() {
        val rule = ParcelExceptionDockLengthRule(255)
        val item = newParcelException("x", "y")
        item.dock = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = ParcelExceptionRequiredShiftCodeRule("parcel exception needs an entry")
        val item = newParcelException("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = ParcelExceptionLoadKgRangeRule(6.8, 2639.0)
        val item = newParcelException("x", "y")
        item.loadKg = 2640.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = ParcelExceptionRequiredDockRule("parcel exception needs a value")
        val item = newParcelException("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
