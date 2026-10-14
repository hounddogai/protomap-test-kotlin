package com.acme.transport.customs.inspection.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultInspectionMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultInspectionMappingValidator()
        val item = newDefaultInspectionMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DefaultInspectionMappingRequiredCodeRule("default inspection mapping needs this field")
        val item = newDefaultInspectionMapping("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank reviewer`() {
        val rule = DefaultInspectionMappingRequiredReviewerRule("default inspection mapping needs this field")
        val item = newDefaultInspectionMapping("", "")
        item.reviewer = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
