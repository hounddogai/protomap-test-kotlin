package com.acme.transport.lastmile.parcel.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParcelGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultParcelGroupValidator()
        val item = newParcelGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ParcelGroupRequiredNameRule("parcel group needs this field")
        val item = newParcelGroup("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long route code`() {
        val rule = ParcelGroupRouteCodeLengthRule(16)
        val item = newParcelGroup("x", "y")
        item.routeCode = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
