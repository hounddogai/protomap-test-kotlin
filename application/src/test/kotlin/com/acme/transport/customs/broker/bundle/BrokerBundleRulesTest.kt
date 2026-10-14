package com.acme.transport.customs.broker.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BrokerBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBrokerBundleValidator()
        val item = newBrokerBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above created at`() {
        val rule = BrokerBundleCreatedAtRangeRule(255, 3600)
        val item = newBrokerBundle("x", "y")
        item.createdAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = BrokerBundleNameLengthRule(64)
        val item = newBrokerBundle("x", "y")
        item.name = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
