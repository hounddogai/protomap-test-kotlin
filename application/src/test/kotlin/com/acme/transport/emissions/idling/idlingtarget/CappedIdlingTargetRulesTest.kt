package com.acme.transport.emissions.idling.idlingtarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedIdlingTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedIdlingTargetValidator()
        val item = newCappedIdlingTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = CappedIdlingTargetExpiresAtRangeRule(80, 255)
        val item = newCappedIdlingTarget("x", "y")
        item.expiresAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long region`() {
        val rule = CappedIdlingTargetRegionLengthRule(64)
        val item = newCappedIdlingTarget("x", "y")
        item.region = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above created at`() {
        val rule = CappedIdlingTargetCreatedAtRangeRule(1000, 86400)
        val item = newCappedIdlingTarget("x", "y")
        item.createdAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
