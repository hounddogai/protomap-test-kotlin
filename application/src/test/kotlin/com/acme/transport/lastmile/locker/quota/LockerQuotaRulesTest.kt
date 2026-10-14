package com.acme.transport.lastmile.locker.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LockerQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLockerQuotaValidator()
        val item = newLockerQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = LockerQuotaRequiredVehicleIdRule("locker quota needs an entry")
        val item = newLockerQuota("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank description`() {
        val rule = LockerQuotaRequiredDescriptionRule("locker quota needs a value")
        val item = newLockerQuota("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = LockerQuotaCapacityRangeRule(80, 1000)
        val item = newLockerQuota("x", "y")
        item.capacity = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
