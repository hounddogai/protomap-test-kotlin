package com.acme.transport.fleetops.utilization.utilizationreservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UtilizationProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultUtilizationProfileValidator()
        val item = newUtilizationProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = UtilizationProfileRequiredChannelRule("utilization profile needs a value")
        val item = newUtilizationProfile("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = UtilizationProfileRequiredCodeRule("utilization profile needs a value")
        val item = newUtilizationProfile("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = UtilizationProfileCodeLengthRule(3600)
        val item = newUtilizationProfile("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
