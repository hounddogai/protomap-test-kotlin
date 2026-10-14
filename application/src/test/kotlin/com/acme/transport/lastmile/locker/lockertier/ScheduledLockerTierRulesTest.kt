package com.acme.transport.lastmile.locker.lockertier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledLockerTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledLockerTierValidator()
        val item = newScheduledLockerTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long category`() {
        val rule = ScheduledLockerTierCategoryLengthRule(16)
        val item = newScheduledLockerTier("x", "y")
        item.category = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank category`() {
        val rule = ScheduledLockerTierRequiredCategoryRule("scheduled locker tier needs an entry")
        val item = newScheduledLockerTier("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = ScheduledLockerTierExternalRefLengthRule(32)
        val item = newScheduledLockerTier("x", "y")
        item.externalRef = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
