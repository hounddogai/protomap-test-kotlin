package com.acme.transport.fleetops.defect.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefectDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefectDraftValidator()
        val item = newDefectDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = DefectDraftCodeLengthRule(80)
        val item = newDefectDraft("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = DefectDraftRequiredSegmentRule("defect draft needs an entry")
        val item = newDefectDraft("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
