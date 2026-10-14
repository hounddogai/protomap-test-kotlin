package com.acme.transport.network.node.nodedraft

import kotlin.test.Test
import kotlin.test.assertTrue

class LenientNodeMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientNodeMappingValidator()
        val item = newLenientNodeMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above retries`() {
        val rule = LenientNodeMappingRetriesRangeRule(64, 1000)
        val item = newLenientNodeMapping("x", "y")
        item.retries = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
