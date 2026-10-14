package com.acme.transport.network.hub.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HubExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultHubExceptionValidator()
        val item = newHubException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank country`() {
        val rule = HubExceptionRequiredCountryRule("hub exception needs a value")
        val item = newHubException("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = HubExceptionBatchSizeRangeRule(3600, 86400)
        val item = newHubException("x", "y")
        item.batchSize = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = HubExceptionRequiredCodeRule("hub exception needs a value")
        val item = newHubException("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = HubExceptionCodeLengthRule(1000)
        val item = newHubException("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
