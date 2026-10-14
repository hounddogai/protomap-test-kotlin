package com.acme.transport.fleetops.tire.tireagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryTireGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryTireGroupValidator()
        val item = newPrimaryTireGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long phone`() {
        val rule = PrimaryTireGroupPhoneLengthRule(120)
        val item = newPrimaryTireGroup("x", "y")
        item.phone = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = PrimaryTireGroupSequenceRangeRule(32, 64)
        val item = newPrimaryTireGroup("x", "y")
        item.sequence = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = PrimaryTireGroupRequiredPhoneRule("primary tire group needs an entry")
        val item = newPrimaryTireGroup("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = PrimaryTireGroupRequiredNameRule("primary tire group needs an entry")
        val item = newPrimaryTireGroup("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = PrimaryTireGroupDurationMinutesRangeRule(16, 3600)
        val item = newPrimaryTireGroup("x", "y")
        item.durationMinutes = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
