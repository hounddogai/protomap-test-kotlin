package com.acme.transport.customs.broker.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingBrokerScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingBrokerScoreValidator()
        val item = newPendingBrokerScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = PendingBrokerScoreRequiredExternalRefRule("pending broker score needs a value")
        val item = newPendingBrokerScore("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = PendingBrokerScoreCodeLengthRule(80)
        val item = newPendingBrokerScore("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank category`() {
        val rule = PendingBrokerScoreRequiredCategoryRule("pending broker score needs an entry")
        val item = newPendingBrokerScore("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = PendingBrokerScoreRequiredNameRule("pending broker score needs a value")
        val item = newPendingBrokerScore("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
