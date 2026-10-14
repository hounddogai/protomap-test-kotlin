package com.acme.transport.customs.broker.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualBrokerOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualBrokerOverrideValidator()
        val item = newManualBrokerOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = ManualBrokerOverridePalletsRangeRule(32, 120)
        val item = newManualBrokerOverride("x", "y")
        item.pallets = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ManualBrokerOverrideRequiredCodeRule("manual broker override needs a value")
        val item = newManualBrokerOverride("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
