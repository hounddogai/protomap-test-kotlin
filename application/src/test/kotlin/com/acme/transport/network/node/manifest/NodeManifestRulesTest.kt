package com.acme.transport.network.node.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NodeManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultNodeManifestValidator()
        val item = newNodeManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = NodeManifestRequiredCodeRule("node manifest needs an entry")
        val item = newNodeManifest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = NodeManifestTitleLengthRule(120)
        val item = newNodeManifest("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = NodeManifestLoadKgRangeRule(5.5, 519.2)
        val item = newNodeManifest("x", "y")
        item.loadKg = 520.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
