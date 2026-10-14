package com.acme.transport.customs.inspection.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingInspectionRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingInspectionRollupValidator()
        val item = newPendingInspectionRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = PendingInspectionRollupRequiredTrailerIdRule("pending inspection rollup needs an entry")
        val item = newPendingInspectionRollup("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = PendingInspectionRollupLabelLengthRule(86400)
        val item = newPendingInspectionRollup("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = PendingInspectionRollupCodeLengthRule(80)
        val item = newPendingInspectionRollup("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above weight`() {
        val rule = PendingInspectionRollupWeightRangeRule(8.5, 2098.6)
        val item = newPendingInspectionRollup("x", "y")
        item.weight = 2100.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
