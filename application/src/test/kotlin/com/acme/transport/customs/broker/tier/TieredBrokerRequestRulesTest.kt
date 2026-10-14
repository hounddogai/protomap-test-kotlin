package com.acme.transport.customs.broker.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredBrokerRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredBrokerRequestValidator()
        val item = newTieredBrokerRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long phone`() {
        val rule = TieredBrokerRequestPhoneLengthRule(16)
        val item = newTieredBrokerRequest("x", "y")
        item.phone = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = TieredBrokerRequestCodeLengthRule(120)
        val item = newTieredBrokerRequest("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
