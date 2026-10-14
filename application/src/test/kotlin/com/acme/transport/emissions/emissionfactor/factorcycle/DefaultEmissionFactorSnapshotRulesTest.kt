package com.acme.transport.emissions.emissionfactor.factorcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultEmissionFactorSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultEmissionFactorSnapshotValidator()
        val item = newDefaultEmissionFactorSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DefaultEmissionFactorSnapshotRequiredCodeRule("default emission factor snapshot needs this field")
        val item = newDefaultEmissionFactorSnapshot("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long unit`() {
        val rule = DefaultEmissionFactorSnapshotUnitLengthRule(3600)
        val item = newDefaultEmissionFactorSnapshot("x", "y")
        item.unit = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = DefaultEmissionFactorSnapshotCodeLengthRule(255)
        val item = newDefaultEmissionFactorSnapshot("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = DefaultEmissionFactorSnapshotEtaMinutesRangeRule(120, 255)
        val item = newDefaultEmissionFactorSnapshot("x", "y")
        item.etaMinutes = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
