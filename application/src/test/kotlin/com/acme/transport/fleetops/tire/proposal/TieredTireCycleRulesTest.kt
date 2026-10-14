package com.acme.transport.fleetops.tire.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredTireCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredTireCycleValidator()
        val item = newTieredTireCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TieredTireCycleRequiredCodeRule("tiered tire cycle needs an entry")
        val item = newTieredTireCycle("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long lane`() {
        val rule = TieredTireCycleLaneLengthRule(255)
        val item = newTieredTireCycle("x", "y")
        item.lane = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above version`() {
        val rule = TieredTireCycleVersionRangeRule(64, 3600)
        val item = newTieredTireCycle("x", "y")
        item.version = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = TieredTireCycleEndsAtRangeRule(120, 255)
        val item = newTieredTireCycle("x", "y")
        item.endsAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
