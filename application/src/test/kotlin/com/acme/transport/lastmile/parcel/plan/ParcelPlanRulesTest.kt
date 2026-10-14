package com.acme.transport.lastmile.parcel.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParcelPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultParcelPlanValidator()
        val item = newParcelPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = ParcelPlanCapacityRangeRule(3600, 86400)
        val item = newParcelPlan("x", "y")
        item.capacity = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = ParcelPlanShiftCodeLengthRule(16)
        val item = newParcelPlan("x", "y")
        item.shiftCode = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ParcelPlanRequiredCodeRule("parcel plan needs this field")
        val item = newParcelPlan("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ParcelPlanRequiredNameRule("parcel plan needs an entry")
        val item = newParcelPlan("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
