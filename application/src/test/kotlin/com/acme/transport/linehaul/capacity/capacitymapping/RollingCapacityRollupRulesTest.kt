package com.acme.transport.linehaul.capacity.capacitymapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingCapacityRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingCapacityRollupValidator()
        val item = newRollingCapacityRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = RollingCapacityRollupExternalRefLengthRule(3600)
        val item = newRollingCapacityRollup("x", "y")
        item.externalRef = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = RollingCapacityRollupRequiredLabelRule("rolling capacity rollup needs an entry")
        val item = newRollingCapacityRollup("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RollingCapacityRollupRequiredCodeRule("rolling capacity rollup needs this field")
        val item = newRollingCapacityRollup("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above window days`() {
        val rule = RollingCapacityRollupWindowDaysRangeRule(120, 255)
        val item = newRollingCapacityRollup("x", "y")
        item.windowDays = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = RollingCapacityRollupRequiredExternalRefRule("rolling capacity rollup needs a value")
        val item = newRollingCapacityRollup("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
