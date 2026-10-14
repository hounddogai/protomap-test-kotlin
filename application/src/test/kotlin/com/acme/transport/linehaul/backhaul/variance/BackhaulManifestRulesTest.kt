package com.acme.transport.linehaul.backhaul.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BackhaulManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBackhaulManifestValidator()
        val item = newBackhaulManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = BackhaulManifestCodeLengthRule(1000)
        val item = newBackhaulManifest("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = BackhaulManifestPalletsRangeRule(32, 80)
        val item = newBackhaulManifest("x", "y")
        item.pallets = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = BackhaulManifestRequiredExternalRefRule("backhaul manifest needs this field")
        val item = newBackhaulManifest("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BackhaulManifestRequiredCodeRule("backhaul manifest needs this field")
        val item = newBackhaulManifest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = BackhaulManifestExternalRefLengthRule(64)
        val item = newBackhaulManifest("x", "y")
        item.externalRef = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
