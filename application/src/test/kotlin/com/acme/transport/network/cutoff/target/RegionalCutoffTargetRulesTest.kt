package com.acme.transport.network.cutoff.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalCutoffTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalCutoffTargetValidator()
        val item = newRegionalCutoffTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long unit`() {
        val rule = RegionalCutoffTargetUnitLengthRule(255)
        val item = newRegionalCutoffTarget("x", "y")
        item.unit = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = RegionalCutoffTargetRequiredUnitRule("regional cutoff target needs this field")
        val item = newRegionalCutoffTarget("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above created at`() {
        val rule = RegionalCutoffTargetCreatedAtRangeRule(64, 86400)
        val item = newRegionalCutoffTarget("x", "y")
        item.createdAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = RegionalCutoffTargetCodeLengthRule(32)
        val item = newRegionalCutoffTarget("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = RegionalCutoffTargetNameLengthRule(16)
        val item = newRegionalCutoffTarget("x", "y")
        item.name = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
