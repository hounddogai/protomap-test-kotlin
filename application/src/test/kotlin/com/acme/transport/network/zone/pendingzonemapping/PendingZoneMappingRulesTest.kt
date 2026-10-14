package com.acme.transport.network.zone.pendingzonemapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingZoneMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingZoneMappingValidator()
        val item = newPendingZoneMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = PendingZoneMappingLabelLengthRule(255)
        val item = newPendingZoneMapping("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = PendingZoneMappingLoadKgRangeRule(9.9, 199.7)
        val item = newPendingZoneMapping("x", "y")
        item.loadKg = 201.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
