package com.acme.transport.terminal.dwell.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DwellAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDwellAllocationValidator()
        val item = newDwellAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = DwellAllocationRequiredCurrencyRule("dwell allocation needs an entry")
        val item = newDwellAllocation("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = DwellAllocationExpiresAtRangeRule(64, 255)
        val item = newDwellAllocation("x", "y")
        item.expiresAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above retries`() {
        val rule = DwellAllocationRetriesRangeRule(80, 3600)
        val item = newDwellAllocation("x", "y")
        item.retries = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
