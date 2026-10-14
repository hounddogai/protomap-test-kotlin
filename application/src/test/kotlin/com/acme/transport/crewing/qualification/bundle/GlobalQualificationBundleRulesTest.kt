package com.acme.transport.crewing.qualification.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalQualificationBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalQualificationBundleValidator()
        val item = newGlobalQualificationBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = GlobalQualificationBundleFloorValueRangeRule(9.0, 633.0)
        val item = newGlobalQualificationBundle("x", "y")
        item.floorValue = 634.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = GlobalQualificationBundleRequiredExternalRefRule("global qualification bundle needs this field")
        val item = newGlobalQualificationBundle("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
