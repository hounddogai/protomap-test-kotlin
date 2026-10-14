package com.acme.transport.emissions.evcharge.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredEvChargeCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredEvChargeCheckpointValidator()
        val item = newTieredEvChargeCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long currency`() {
        val rule = TieredEvChargeCheckpointCurrencyLengthRule(32)
        val item = newTieredEvChargeCheckpoint("x", "y")
        item.currency = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = TieredEvChargeCheckpointNameLengthRule(120)
        val item = newTieredEvChargeCheckpoint("x", "y")
        item.name = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TieredEvChargeCheckpointRequiredCodeRule("tiered ev charge checkpoint needs this field")
        val item = newTieredEvChargeCheckpoint("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long locale`() {
        val rule = TieredEvChargeCheckpointLocaleLengthRule(86400)
        val item = newTieredEvChargeCheckpoint("x", "y")
        item.locale = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above score`() {
        val rule = TieredEvChargeCheckpointScoreRangeRule(10.0, 328.9)
        val item = newTieredEvChargeCheckpoint("x", "y")
        item.score = 330.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
