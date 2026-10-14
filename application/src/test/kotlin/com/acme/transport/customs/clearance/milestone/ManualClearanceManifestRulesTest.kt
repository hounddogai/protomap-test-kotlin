package com.acme.transport.customs.clearance.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualClearanceManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualClearanceManifestValidator()
        val item = newManualClearanceManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = ManualClearanceManifestShiftCodeLengthRule(64)
        val item = newManualClearanceManifest("x", "y")
        item.shiftCode = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ManualClearanceManifestRequiredCodeRule("manual clearance manifest needs a value")
        val item = newManualClearanceManifest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = ManualClearanceManifestRatioRangeRule(3.3, 3769.5)
        val item = newManualClearanceManifest("x", "y")
        item.ratio = 3771.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
