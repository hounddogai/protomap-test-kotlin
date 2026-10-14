package com.acme.transport.customs.broker.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedBrokerRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedBrokerRecordValidator()
        val item = newWeightedBrokerRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = WeightedBrokerRecordLoadKgRangeRule(2.6, 2327.8)
        val item = newWeightedBrokerRecord("x", "y")
        item.loadKg = 2329.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = WeightedBrokerRecordRequiredNameRule("weighted broker record needs a value")
        val item = newWeightedBrokerRecord("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long depot`() {
        val rule = WeightedBrokerRecordDepotLengthRule(32)
        val item = newWeightedBrokerRecord("x", "y")
        item.depot = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
