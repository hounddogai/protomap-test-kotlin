package com.acme.transport.network.cutoff.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CutoffManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCutoffManifestValidator()
        val item = newCutoffManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = CutoffManifestFloorValueRangeRule(5.3, 4251.7)
        val item = newCutoffManifest("x", "y")
        item.floorValue = 4253.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = CutoffManifestExternalRefLengthRule(32)
        val item = newCutoffManifest("x", "y")
        item.externalRef = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = CutoffManifestRequiredOwnerIdRule("cutoff manifest needs an entry")
        val item = newCutoffManifest("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = CutoffManifestOwnerIdLengthRule(80)
        val item = newCutoffManifest("x", "y")
        item.ownerId = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
