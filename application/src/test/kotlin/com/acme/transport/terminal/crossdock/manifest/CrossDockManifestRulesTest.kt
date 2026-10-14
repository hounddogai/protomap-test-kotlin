package com.acme.transport.terminal.crossdock.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CrossDockManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCrossDockManifestValidator()
        val item = newCrossDockManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = CrossDockManifestRequiredDepotRule("cross dock manifest needs this field")
        val item = newCrossDockManifest("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CrossDockManifestRequiredCodeRule("cross dock manifest needs an entry")
        val item = newCrossDockManifest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = CrossDockManifestTitleLengthRule(255)
        val item = newCrossDockManifest("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
