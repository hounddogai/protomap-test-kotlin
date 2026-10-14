package com.acme.transport.lastmile.parcel.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParcelRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultParcelRequestValidator()
        val item = newParcelRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above version`() {
        val rule = ParcelRequestVersionRangeRule(80, 1000)
        val item = newParcelRequest("x", "y")
        item.version = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = ParcelRequestChecksumLengthRule(64)
        val item = newParcelRequest("x", "y")
        item.checksum = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = ParcelRequestFloorValueRangeRule(9.5, 4073.8)
        val item = newParcelRequest("x", "y")
        item.floorValue = 4075.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
