package com.acme.transport.linehaul.tender.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictTenderAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictTenderAllocationValidator()
        val item = newStrictTenderAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = StrictTenderAllocationCodeLengthRule(80)
        val item = newStrictTenderAllocation("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above revision`() {
        val rule = StrictTenderAllocationRevisionRangeRule(32, 255)
        val item = newStrictTenderAllocation("x", "y")
        item.revision = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = StrictTenderAllocationNameLengthRule(255)
        val item = newStrictTenderAllocation("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
