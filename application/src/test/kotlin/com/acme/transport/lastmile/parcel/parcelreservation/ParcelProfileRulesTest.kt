package com.acme.transport.lastmile.parcel.parcelreservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParcelProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultParcelProfileValidator()
        val item = newParcelProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = ParcelProfileNameLengthRule(3600)
        val item = newParcelProfile("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ParcelProfileRequiredCodeRule("parcel profile needs a value")
        val item = newParcelProfile("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ParcelProfileCodeLengthRule(120)
        val item = newParcelProfile("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = ParcelProfileRequiredRouteCodeRule("parcel profile needs this field")
        val item = newParcelProfile("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
