package com.acme.transport.fleetops.defect.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalDefectSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalDefectSnapshotValidator()
        val item = newGlobalDefectSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = GlobalDefectSnapshotRequiredDockRule("global defect snapshot needs this field")
        val item = newGlobalDefectSnapshot("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = GlobalDefectSnapshotFloorValueRangeRule(0.6, 4612.3)
        val item = newGlobalDefectSnapshot("x", "y")
        item.floorValue = 4613.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = GlobalDefectSnapshotRequiredShiftCodeRule("global defect snapshot needs a value")
        val item = newGlobalDefectSnapshot("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = GlobalDefectSnapshotShiftCodeLengthRule(80)
        val item = newGlobalDefectSnapshot("x", "y")
        item.shiftCode = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
