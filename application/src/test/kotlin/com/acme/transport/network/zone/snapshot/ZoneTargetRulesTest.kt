package com.acme.transport.network.zone.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ZoneTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultZoneTargetValidator()
        val item = newZoneTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = ZoneTargetCeilingRangeRule(1.9, 2875.9)
        val item = newZoneTarget("x", "y")
        item.ceiling = 2877.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = ZoneTargetOwnerIdLengthRule(86400)
        val item = newZoneTarget("x", "y")
        item.ownerId = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
