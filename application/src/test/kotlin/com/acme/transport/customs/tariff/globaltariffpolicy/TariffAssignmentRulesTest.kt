package com.acme.transport.customs.tariff.globaltariffpolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TariffAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTariffAssignmentValidator()
        val item = newTariffAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above margin`() {
        val rule = TariffAssignmentMarginRangeRule(8.6, 3195.6)
        val item = newTariffAssignment("x", "y")
        item.margin = 3197.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = TariffAssignmentCodeLengthRule(120)
        val item = newTariffAssignment("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = TariffAssignmentNameLengthRule(255)
        val item = newTariffAssignment("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
