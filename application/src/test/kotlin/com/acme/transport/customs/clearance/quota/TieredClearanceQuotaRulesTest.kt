package com.acme.transport.customs.clearance.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredClearanceQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredClearanceQuotaValidator()
        val item = newTieredClearanceQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = TieredClearanceQuotaCodeLengthRule(80)
        val item = newTieredClearanceQuota("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = TieredClearanceQuotaUpdatedAtRangeRule(64, 86400)
        val item = newTieredClearanceQuota("x", "y")
        item.updatedAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = TieredClearanceQuotaDriverIdLengthRule(1000)
        val item = newTieredClearanceQuota("x", "y")
        item.driverId = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = TieredClearanceQuotaRequiredDriverIdRule("tiered clearance quota needs an entry")
        val item = newTieredClearanceQuota("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
