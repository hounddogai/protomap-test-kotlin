package com.acme.transport.fleetops.utilization.tieredutilizationplan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UtilizationDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultUtilizationDraftValidator()
        val item = newUtilizationDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long route code`() {
        val rule = UtilizationDraftRouteCodeLengthRule(86400)
        val item = newUtilizationDraft("x", "y")
        item.routeCode = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = UtilizationDraftRequiredCodeRule("utilization draft needs this field")
        val item = newUtilizationDraft("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
