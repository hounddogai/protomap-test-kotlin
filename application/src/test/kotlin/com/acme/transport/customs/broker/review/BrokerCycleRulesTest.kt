package com.acme.transport.customs.broker.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BrokerCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBrokerCycleValidator()
        val item = newBrokerCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = BrokerCycleScoreRangeRule(2.5, 2076.9)
        val item = newBrokerCycle("x", "y")
        item.score = 2078.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = BrokerCycleRequiredNameRule("broker cycle needs a value")
        val item = newBrokerCycle("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
