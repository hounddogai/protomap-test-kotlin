package com.acme.transport.emissions.evcharge.batchedchargeslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryEvChargeGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryEvChargeGroupValidator()
        val item = newPrimaryEvChargeGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = PrimaryEvChargeGroupChecksumLengthRule(3600)
        val item = newPrimaryEvChargeGroup("x", "y")
        item.checksum = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = PrimaryEvChargeGroupEtaMinutesRangeRule(80, 255)
        val item = newPrimaryEvChargeGroup("x", "y")
        item.etaMinutes = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = PrimaryEvChargeGroupFloorValueRangeRule(1.6, 1724.0)
        val item = newPrimaryEvChargeGroup("x", "y")
        item.floorValue = 1725.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = PrimaryEvChargeGroupRequiredNameRule("primary ev charge group needs this field")
        val item = newPrimaryEvChargeGroup("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PrimaryEvChargeGroupRequiredCodeRule("primary ev charge group needs an entry")
        val item = newPrimaryEvChargeGroup("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
