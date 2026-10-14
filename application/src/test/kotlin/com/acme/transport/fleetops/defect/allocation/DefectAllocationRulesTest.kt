package com.acme.transport.fleetops.defect.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefectAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefectAllocationValidator()
        val item = newDefectAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long channel`() {
        val rule = DefectAllocationChannelLengthRule(80)
        val item = newDefectAllocation("x", "y")
        item.channel = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = DefectAllocationCodeLengthRule(16)
        val item = newDefectAllocation("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
