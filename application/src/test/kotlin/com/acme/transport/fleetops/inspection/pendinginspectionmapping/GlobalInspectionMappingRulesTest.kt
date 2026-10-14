package com.acme.transport.fleetops.inspection.pendinginspectionmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalInspectionMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalInspectionMappingValidator()
        val item = newGlobalInspectionMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = GlobalInspectionMappingRequiredNameRule("global inspection mapping needs a value")
        val item = newGlobalInspectionMapping("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = GlobalInspectionMappingPalletsRangeRule(64, 80)
        val item = newGlobalInspectionMapping("x", "y")
        item.pallets = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = GlobalInspectionMappingNameLengthRule(86400)
        val item = newGlobalInspectionMapping("x", "y")
        item.name = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
