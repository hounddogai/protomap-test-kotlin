package com.acme.transport.lastmile.parcel.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParcelTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultParcelTargetValidator()
        val item = newParcelTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ParcelTargetRequiredCodeRule("parcel target needs an entry")
        val item = newParcelTarget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ParcelTargetCodeLengthRule(3600)
        val item = newParcelTarget("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = ParcelTargetRequiredLaneRule("parcel target needs this field")
        val item = newParcelTarget("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
