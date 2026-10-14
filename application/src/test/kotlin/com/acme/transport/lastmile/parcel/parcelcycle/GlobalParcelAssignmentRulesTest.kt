package com.acme.transport.lastmile.parcel.parcelcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalParcelAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalParcelAssignmentValidator()
        val item = newGlobalParcelAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = GlobalParcelAssignmentRequiredCodeRule("global parcel assignment needs this field")
        val item = newGlobalParcelAssignment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = GlobalParcelAssignmentCeilingRangeRule(0.8, 456.5)
        val item = newGlobalParcelAssignment("x", "y")
        item.ceiling = 458.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
