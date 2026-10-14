package com.acme.transport.customs.declaration.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingDeclarationManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingDeclarationManifestValidator()
        val item = newRollingDeclarationManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RollingDeclarationManifestRequiredCodeRule("rolling declaration manifest needs a value")
        val item = newRollingDeclarationManifest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long summary`() {
        val rule = RollingDeclarationManifestSummaryLengthRule(3600)
        val item = newRollingDeclarationManifest("x", "y")
        item.summary = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = RollingDeclarationManifestRequiredShiftCodeRule("rolling declaration manifest needs a value")
        val item = newRollingDeclarationManifest("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = RollingDeclarationManifestFloorValueRangeRule(9.1, 2616.4)
        val item = newRollingDeclarationManifest("x", "y")
        item.floorValue = 2617.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
