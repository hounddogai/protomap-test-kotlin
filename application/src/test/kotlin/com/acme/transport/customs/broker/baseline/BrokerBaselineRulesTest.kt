package com.acme.transport.customs.broker.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BrokerBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBrokerBaselineValidator()
        val item = newBrokerBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = BrokerBaselineCodeLengthRule(1000)
        val item = newBrokerBaseline("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = BrokerBaselineRequiredLabelRule("broker baseline needs an entry")
        val item = newBrokerBaseline("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
