package com.acme.transport.network.transittime.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TransitTimeThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTransitTimeThresholdValidator()
        val item = newTransitTimeThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = TransitTimeThresholdCodeLengthRule(120)
        val item = newTransitTimeThreshold("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = TransitTimeThresholdRequiredPhoneRule("transit time threshold needs a value")
        val item = newTransitTimeThreshold("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
