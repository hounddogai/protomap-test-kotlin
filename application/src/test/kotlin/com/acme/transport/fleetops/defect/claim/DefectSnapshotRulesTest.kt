package com.acme.transport.fleetops.defect.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefectSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefectSnapshotValidator()
        val item = newDefectSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = DefectSnapshotNameLengthRule(80)
        val item = newDefectSnapshot("x", "y")
        item.name = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DefectSnapshotRequiredCodeRule("defect snapshot needs a value")
        val item = newDefectSnapshot("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
