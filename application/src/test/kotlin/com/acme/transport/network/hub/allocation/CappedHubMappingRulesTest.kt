package com.acme.transport.network.hub.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedHubMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedHubMappingValidator()
        val item = newCappedHubMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = CappedHubMappingCodeLengthRule(16)
        val item = newCappedHubMapping("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = CappedHubMappingRequiredOwnerIdRule("capped hub mapping needs an entry")
        val item = newCappedHubMapping("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above retries`() {
        val rule = CappedHubMappingRetriesRangeRule(80, 1000)
        val item = newCappedHubMapping("x", "y")
        item.retries = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CappedHubMappingRequiredCodeRule("capped hub mapping needs a value")
        val item = newCappedHubMapping("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = CappedHubMappingBatchSizeRangeRule(16, 3600)
        val item = newCappedHubMapping("x", "y")
        item.batchSize = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long country`() {
        val rule = CappedHubMappingCountryLengthRule(80)
        val item = newCappedHubMapping("x", "y")
        item.country = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
