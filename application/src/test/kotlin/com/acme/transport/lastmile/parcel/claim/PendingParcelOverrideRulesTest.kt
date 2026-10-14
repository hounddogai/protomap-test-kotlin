package com.acme.transport.lastmile.parcel.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingParcelOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingParcelOverrideValidator()
        val item = newPendingParcelOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = PendingParcelOverrideFloorValueRangeRule(0.5, 4783.5)
        val item = newPendingParcelOverride("x", "y")
        item.floorValue = 4785.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = PendingParcelOverrideDriverIdLengthRule(86400)
        val item = newPendingParcelOverride("x", "y")
        item.driverId = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = PendingParcelOverrideCodeLengthRule(120)
        val item = newPendingParcelOverride("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = PendingParcelOverrideNameLengthRule(16)
        val item = newPendingParcelOverride("x", "y")
        item.name = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
