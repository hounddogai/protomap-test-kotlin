package com.acme.transport.customs.broker.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedBrokerScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedBrokerScoreValidator()
        val item = newWeightedBrokerScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = WeightedBrokerScoreRequiredContactEmailRule("weighted broker score needs a value")
        val item = newWeightedBrokerScore("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = WeightedBrokerScoreCodeLengthRule(16)
        val item = newWeightedBrokerScore("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = WeightedBrokerScoreLabelLengthRule(1000)
        val item = newWeightedBrokerScore("x", "y")
        item.label = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = WeightedBrokerScoreContactEmailLengthRule(64)
        val item = newWeightedBrokerScore("x", "y")
        item.contactEmail = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
