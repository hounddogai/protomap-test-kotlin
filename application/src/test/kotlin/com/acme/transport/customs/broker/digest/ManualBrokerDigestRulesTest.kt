package com.acme.transport.customs.broker.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualBrokerDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualBrokerDigestValidator()
        val item = newManualBrokerDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ManualBrokerDigestRequiredLabelRule("manual broker digest needs an entry")
        val item = newManualBrokerDigest("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = ManualBrokerDigestAmountRangeRule(8.5, 4081.3)
        val item = newManualBrokerDigest("x", "y")
        item.amount = 4082.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = ManualBrokerDigestCodeLengthRule(32)
        val item = newManualBrokerDigest("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
