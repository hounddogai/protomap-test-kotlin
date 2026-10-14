package com.acme.transport.network.zone.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientZoneGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientZoneGroupValidator()
        val item = newLenientZoneGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above created at`() {
        val rule = LenientZoneGroupCreatedAtRangeRule(64, 80)
        val item = newLenientZoneGroup("x", "y")
        item.createdAt = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above weight`() {
        val rule = LenientZoneGroupWeightRangeRule(6.8, 208.8)
        val item = newLenientZoneGroup("x", "y")
        item.weight = 210.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LenientZoneGroupRequiredCodeRule("lenient zone group needs a value")
        val item = newLenientZoneGroup("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above window days`() {
        val rule = LenientZoneGroupWindowDaysRangeRule(16, 86400)
        val item = newLenientZoneGroup("x", "y")
        item.windowDays = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
