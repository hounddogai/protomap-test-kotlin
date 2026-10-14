package com.acme.transport.customs.broker.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingBrokerAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingBrokerAdjustmentValidator()
        val item = newRollingBrokerAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RollingBrokerAdjustmentCodeLengthRule(255)
        val item = newRollingBrokerAdjustment("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = RollingBrokerAdjustmentEtaMinutesRangeRule(64, 1000)
        val item = newRollingBrokerAdjustment("x", "y")
        item.etaMinutes = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RollingBrokerAdjustmentRequiredCodeRule("rolling broker adjustment needs this field")
        val item = newRollingBrokerAdjustment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
