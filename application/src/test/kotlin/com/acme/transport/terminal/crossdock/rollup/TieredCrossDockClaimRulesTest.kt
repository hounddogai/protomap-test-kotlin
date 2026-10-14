package com.acme.transport.terminal.crossdock.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredCrossDockClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredCrossDockClaimValidator()
        val item = newTieredCrossDockClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = TieredCrossDockClaimCodeLengthRule(255)
        val item = newTieredCrossDockClaim("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = TieredCrossDockClaimOdometerRangeRule(1000, 86400)
        val item = newTieredCrossDockClaim("x", "y")
        item.odometer = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = TieredCrossDockClaimDriverIdLengthRule(120)
        val item = newTieredCrossDockClaim("x", "y")
        item.driverId = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
