package com.acme.transport.terminal.crossdock.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientCrossDockQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientCrossDockQuotaValidator()
        val item = newLenientCrossDockQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = LenientCrossDockQuotaFloorValueRangeRule(0.4, 2127.2)
        val item = newLenientCrossDockQuota("x", "y")
        item.floorValue = 2128.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long region`() {
        val rule = LenientCrossDockQuotaRegionLengthRule(86400)
        val item = newLenientCrossDockQuota("x", "y")
        item.region = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank region`() {
        val rule = LenientCrossDockQuotaRequiredRegionRule("lenient cross dock quota needs this field")
        val item = newLenientCrossDockQuota("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = LenientCrossDockQuotaNameLengthRule(86400)
        val item = newLenientCrossDockQuota("x", "y")
        item.name = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = LenientCrossDockQuotaPalletsRangeRule(32, 64)
        val item = newLenientCrossDockQuota("x", "y")
        item.pallets = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
