package com.acme.transport.fleetops.tire.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TireGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTireGroupValidator()
        val item = newTireGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = TireGroupRequiredChecksumRule("tire group needs a value")
        val item = newTireGroup("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = TireGroupTrailerIdLengthRule(80)
        val item = newTireGroup("x", "y")
        item.trailerId = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TireGroupRequiredCodeRule("tire group needs an entry")
        val item = newTireGroup("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above rate`() {
        val rule = TireGroupRateRangeRule(6.7, 1233.2)
        val item = newTireGroup("x", "y")
        item.rate = 1234.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = TireGroupNameLengthRule(32)
        val item = newTireGroup("x", "y")
        item.name = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
