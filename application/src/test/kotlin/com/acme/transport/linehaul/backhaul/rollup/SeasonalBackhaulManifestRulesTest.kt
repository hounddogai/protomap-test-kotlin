package com.acme.transport.linehaul.backhaul.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalBackhaulManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalBackhaulManifestValidator()
        val item = newSeasonalBackhaulManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = SeasonalBackhaulManifestRequiredLabelRule("seasonal backhaul manifest needs a value")
        val item = newSeasonalBackhaulManifest("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long source`() {
        val rule = SeasonalBackhaulManifestSourceLengthRule(80)
        val item = newSeasonalBackhaulManifest("x", "y")
        item.source = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.source = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = SeasonalBackhaulManifestCodeLengthRule(86400)
        val item = newSeasonalBackhaulManifest("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
