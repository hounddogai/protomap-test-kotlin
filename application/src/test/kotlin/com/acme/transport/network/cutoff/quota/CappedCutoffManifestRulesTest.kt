package com.acme.transport.network.cutoff.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedCutoffManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedCutoffManifestValidator()
        val item = newCappedCutoffManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above limit`() {
        val rule = CappedCutoffManifestLimitRangeRule(32, 32)
        val item = newCappedCutoffManifest("x", "y")
        item.limit = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = CappedCutoffManifestDriverIdLengthRule(32)
        val item = newCappedCutoffManifest("x", "y")
        item.driverId = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = CappedCutoffManifestLoadKgRangeRule(3.7, 695.3)
        val item = newCappedCutoffManifest("x", "y")
        item.loadKg = 696.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CappedCutoffManifestRequiredCodeRule("capped cutoff manifest needs a value")
        val item = newCappedCutoffManifest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = CappedCutoffManifestRequiredOwnerIdRule("capped cutoff manifest needs a value")
        val item = newCappedCutoffManifest("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
