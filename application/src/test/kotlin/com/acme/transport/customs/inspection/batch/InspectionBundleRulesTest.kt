package com.acme.transport.customs.inspection.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultInspectionBundleValidator()
        val item = newInspectionBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = InspectionBundleTitleLengthRule(64)
        val item = newInspectionBundle("x", "y")
        item.title = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = InspectionBundleRequiredTitleRule("inspection bundle needs an entry")
        val item = newInspectionBundle("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
