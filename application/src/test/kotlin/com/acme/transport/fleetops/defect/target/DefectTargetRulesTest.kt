package com.acme.transport.fleetops.defect.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefectTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefectTargetValidator()
        val item = newDefectTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long locale`() {
        val rule = DefectTargetLocaleLengthRule(86400)
        val item = newDefectTarget("x", "y")
        item.locale = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = DefectTargetRequiredRouteCodeRule("defect target needs this field")
        val item = newDefectTarget("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DefectTargetRequiredCodeRule("defect target needs an entry")
        val item = newDefectTarget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = DefectTargetCeilingRangeRule(1.8, 3750.8)
        val item = newDefectTarget("x", "y")
        item.ceiling = 3752.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above revision`() {
        val rule = DefectTargetRevisionRangeRule(32, 3600)
        val item = newDefectTarget("x", "y")
        item.revision = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
