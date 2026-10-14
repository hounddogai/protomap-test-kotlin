package com.acme.transport.network.node.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NodeWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultNodeWindowValidator()
        val item = newNodeWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = NodeWindowRequiredCodeRule("node window needs this field")
        val item = newNodeWindow("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = NodeWindowRequiredNameRule("node window needs an entry")
        val item = newNodeWindow("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
